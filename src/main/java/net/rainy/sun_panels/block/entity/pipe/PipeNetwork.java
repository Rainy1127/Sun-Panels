package net.rainy.sun_panels.block.entity.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A group of connected pipes that acts as one passive conduit.
 * <p>
 * Pipes do not tick and do not buffer energy. When something pushes energy into any pipe face,
 * the network hands it straight to every energy block touching the network, limited per tick
 * by the slowest pipe in the network. The pipe layout is discovered once with a flood fill and
 * cached until a pipe is placed, broken, loaded or unloaded.
 */
public final class PipeNetwork {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final List<BaseEnergyPipeBlockEntity> members = new ArrayList<>();
    private final List<Endpoint> endpoints = new ArrayList<>();
    private final List<IEnergyStorage> receiversScratch = new ArrayList<>();
    private final ServerLevel level;
    private int transferRate = Integer.MAX_VALUE;
    private boolean valid = true;
    private boolean distributing;
    private long budgetTick = Long.MIN_VALUE;
    private int transferredThisTick;
    private int roundRobinStart;

    private PipeNetwork(ServerLevel level) {
        this.level = level;
    }

    static PipeNetwork build(ServerLevel level, BaseEnergyPipeBlockEntity origin) {
        PipeNetwork network = new PipeNetwork(level);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BaseEnergyPipeBlockEntity> queue = new ArrayDeque<>();
        visited.add(origin.getBlockPos());
        queue.add(origin);

        while (!queue.isEmpty()) {
            BaseEnergyPipeBlockEntity pipe = queue.poll();
            network.members.add(pipe);
            network.transferRate = Math.min(network.transferRate, pipe.getTransferRate());
            pipe.attachNetwork(network);

            BlockPos pipePos = pipe.getBlockPos();
            for (Direction side : DIRECTIONS) {
                BlockPos neighborPos = pipePos.relative(side);
                BlockEntity neighbor = level.isLoaded(neighborPos) ? level.getBlockEntity(neighborPos) : null;
                if (neighbor instanceof BaseEnergyPipeBlockEntity neighborPipe && !neighborPipe.isRemoved()) {
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

    private void addEndpoint(BaseEnergyPipeBlockEntity pipe, Direction side, BlockPos neighborPos) {
        // Only blocks with a block entity can expose energy. Faces touching air or plain blocks get no
        // cache; the pipe block invalidates the network if such a face later gains a block entity.
        boolean candidate = !level.isLoaded(neighborPos) || level.getBlockState(neighborPos).hasBlockEntity();
        if (!candidate) {
            return;
        }
        pipe.markEndpointFace(side);
        BlockCapabilityCache<IEnergyStorage, Direction> cache = BlockCapabilityCache.create(
                Capabilities.EnergyStorage.BLOCK, level, neighborPos, side.getOpposite(),
                () -> this.valid && !pipe.isRemoved(), () -> {});
        endpoints.add(new Endpoint(pipe.getBlockPos(), side, cache));
    }

    boolean isValid() {
        return valid;
    }

    int getTransferRate() {
        return transferRate;
    }

    void invalidate() {
        if (!valid) {
            return;
        }
        valid = false;
        for (BaseEnergyPipeBlockEntity member : members) {
            member.detachNetwork(this);
        }
        members.clear();
        endpoints.clear();
    }

    /**
     * Pushes energy that entered the network through {@code sourceSide} of the pipe at {@code sourcePipe}
     * out to every connected receiver except the block it came from.
     */
    int distribute(int amount, BlockPos sourcePipe, Direction sourceSide, boolean simulate) {
        if (!valid || distributing || amount <= 0) {
            return 0;
        }

        long now = level.getGameTime();
        if (now != budgetTick) {
            budgetTick = now;
            transferredThisTick = 0;
        }

        int budget = Math.min(amount, transferRate - transferredThisTick);
        if (budget <= 0) {
            return 0;
        }

        distributing = true;
        try {
            for (Endpoint endpoint : endpoints) {
                if (endpoint.side == sourceSide && endpoint.pipePos.equals(sourcePipe)) {
                    continue;
                }
                IEnergyStorage receiver = endpoint.cache.getCapability();
                if (receiver != null && receiver.canReceive() && !(receiver instanceof PipeFaceHandler)) {
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
                IEnergyStorage receiver = receiversScratch.get((start + i) % count);
                int remainingReceivers = count - i;
                int share = (budget - sent + remainingReceivers - 1) / remainingReceivers;
                sent += receiver.receiveEnergy(share, simulate);
            }

            if (!simulate) {
                transferredThisTick += sent;
                roundRobinStart = (start + 1) % count;
            }
            return sent;
        } finally {
            receiversScratch.clear();
            distributing = false;
        }
    }

    private record Endpoint(BlockPos pipePos, Direction side, BlockCapabilityCache<IEnergyStorage, Direction> cache) {
    }
}
