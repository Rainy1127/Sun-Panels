package net.rainy.sun_panels.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.rainy.sun_panels.energy.PipeFaceHandler;
import net.rainy.sun_panels.energy.PipeNetwork;
import net.rainy.sun_panels.registry.SPRegistries;

/**
 * Pipe block entity. It never ticks; it only owns the per-face energy handlers and a reference to the
 * cached {@link PipeNetwork}, which is rebuilt lazily after the pipe layout changes.
 */
public class EnergyPipeBlockEntity extends BlockEntity {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final PipeTier tier;
    private final PipeFaceHandler[] faceHandlers = new PipeFaceHandler[DIRECTIONS.length];
    private final PipeFaceHandler unsidedHandler;
    private PipeNetwork network;

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(SPRegistries.ENERGY_PIPE_BE.get(), pos, state);
        this.tier = state.getBlock() instanceof EnergyPipeBlock block ? block.tier() : PipeTier.BASIC;
        for (Direction side : DIRECTIONS) {
            faceHandlers[side.ordinal()] = new PipeFaceHandler(this, side);
        }
        this.unsidedHandler = new PipeFaceHandler(this, null);
    }

    public PipeTier getTier() {
        return tier;
    }

    public EnergyHandler getEnergyHandler(Direction side) {
        return side == null ? unsidedHandler : faceHandlers[side.ordinal()];
    }

    public PipeNetwork getOrBuildNetwork() {
        if (isRemoved() || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        if (network == null || !network.isValid()) {
            network = PipeNetwork.build(serverLevel, this);
        }
        return network;
    }

    public void attachNetwork(PipeNetwork newNetwork) {
        PipeNetwork previous = this.network;
        this.network = newNetwork;
        if (previous != null && previous != newNetwork) {
            previous.invalidate();
        }
    }

    public void detachNetwork(PipeNetwork oldNetwork) {
        if (this.network == oldNetwork) {
            this.network = null;
        }
    }

    private void invalidateNetwork() {
        if (network != null) {
            PipeNetwork old = network;
            network = null;
            old.invalidate();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            // A new or freshly loaded pipe may join or merge existing networks.
            for (Direction side : DIRECTIONS) {
                BlockPos neighborPos = worldPosition.relative(side);
                if (level.isLoaded(neighborPos) && level.getBlockEntity(neighborPos) instanceof EnergyPipeBlockEntity neighbor) {
                    neighbor.invalidateNetwork();
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        invalidateNetwork();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        invalidateNetwork();
    }
}
