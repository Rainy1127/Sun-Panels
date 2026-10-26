package net.rainy.sun_panels.energy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.rainy.sun_panels.block.EnergyPipeBlockEntity;

/**
 * A group of connected pipes that acts as one passive conduit.
 *
 * <p>Pipes never tick and never buffer energy. When energy is inserted into any pipe face, the network
 * hands it straight to every energy block touching the network (except the one it came from), limited
 * per tick by the slowest pipe in the network. The layout is found once with a flood fill and cached
 * until a pipe is placed, broken, loaded or unloaded. Neighbouring machines are watched through
 * {@link BlockCapabilityCache}s, so placing or removing a machine next to a pipe needs no rebuild.
 */
public final class PipeNetwork {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final ServerLevel level;
    private final List<EnergyPipeBlockEntity> members = new ArrayList<>();
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final List<EnergyHandler> receiversScratch = new ArrayList<>();
    private final BudgetJournal budgetJournal = new BudgetJournal();
    private int transferRate = Integer.MAX_VALUE;
    private boolean valid = true;
    private boolean distributing;
    private long budgetTick = Long.MIN_VALUE;
    private int transferredThisTick;
    private int roundRobinStart;

    private PipeNetwork(ServerLevel level) {
        this.level = level;
    }

    public static PipeNetwork build(ServerLevel level, EnergyPipeBlockEntity origin) {
        PipeNetwork network = new PipeNetwork(level);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<EnergyPipeBlockEntity> queue = new ArrayDeque<>();
        visited.add(origin.getBlockPos());
        queue.add(origin);

        while (!queue.isEmpty()) {
            EnergyPipeBlockEntity pipe = queue.poll();
            network.members.add(pipe);
            network.transferRate = Math.min(network.transferRate, pipe.getTier().transferRate());
            pipe.attachNetwork(network);

            BlockPos pipePos = pipe.getBlockPos();
            for (Direction side : DIRECTIONS) {
                BlockPos neighborPos = pipePos.relative(side);
                BlockEntity neighbor = level.isLoaded(neighborPos) ? level.getBlockEntity(neighborPos) : null;
                if (neighbor instanceof EnergyPipeBlockEntity neighborPipe && !neighborPipe.isRemoved()) {
                    if (visited.add(neighborPos)) {
                        queue.add(neighborPipe);
                    }
                } else {
                    network.addEndpoint(pipe, side, neighborPos);
                }
            }
        }
        return network;
    }

    private void addEndpoint(EnergyPipeBlockEntity pipe, Direction side, BlockPos neighborPos) {
        BlockCapabilityCache<EnergyHandler, Direction> cache = BlockCapabilityCache.create(
                Capabilities.Energy.BLOCK, level, neighborPos, side.getOpposite(),
                () -> this.valid && !pipe.isRemoved(), () -> {});
        endpoints.add(new Endpoint(pipe.getBlockPos(), side, cache));
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        if (!valid) {
            return;
        }
        valid = false;
        for (EnergyPipeBlockEntity member : members) {
            member.detachNetwork(this);
        }
        members.clear();
        endpoints.clear();
    }

    /**
     * Sends energy that entered through {@code sourceSide} of the pipe at {@code sourcePipe} to every other
     * receiver of the network, inside the caller's transaction. The per-tick budget is journaled too,
     * so a simulated or aborted transfer does not use it up.
     */
    int distribute(int amount, BlockPos sourcePipe, Direction sourceSide, TransactionContext transaction) {
        if (!valid || distributing || amount <= 0) {
            return 0;
        }

        long now = level.getGameTime();
        int used = budgetTick == now ? transferredThisTick : 0;
        int budget = Math.min(amount, transferRate - used);
        if (budget <= 0) {
            return 0;
        }

        distributing = true;
        try {
            for (Endpoint endpoint : endpoints) {
                if (endpoint.side() == sourceSide && endpoint.pipePos().equals(sourcePipe)) {
                    continue;
                }
                EnergyHandler receiver = endpoint.cache().getCapability();
                if (receiver != null && !(receiver instanceof PipeFaceHandler)) {
                    receiversScratch.add(receiver);
                }
            }

            int count = receiversScratch.size();
            if (count == 0) {
                return 0;
            }

            int sent = 0;
            int start = roundRobinStart % count;
            for (int i = 0; i < count && sent < budget; i++) {
                EnergyHandler receiver = receiversScratch.get((start + i) % count);
                int remainingReceivers = count - i;
                int share = (budget - sent + remainingReceivers - 1) / remainingReceivers;
                sent += receiver.insert(share, transaction);
            }

            if (sent > 0) {
                budgetJournal.updateSnapshots(transaction);
                budgetTick = now;
                transferredThisTick = used + sent;
                roundRobinStart = (start + 1) % count;
            }
            return sent;
        } finally {
            receiversScratch.clear();
            distributing = false;
        }
    }

    private record Endpoint(BlockPos pipePos, Direction side, BlockCapabilityCache<EnergyHandler, Direction> cache) {
    }

    private record Budget(long tick, int transferred) {
    }

    private final class BudgetJournal extends SnapshotJournal<Budget> {
        @Override
        protected Budget createSnapshot() {
            return new Budget(budgetTick, transferredThisTick);
        }

        @Override
        protected void revertToSnapshot(Budget snapshot) {
            budgetTick = snapshot.tick();
            transferredThisTick = snapshot.transferred();
        }
    }
}
