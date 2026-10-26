package net.rainy.sun_panels.block.entity.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.rainy.sun_panels.registry.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * Energy pipe block entity. Pipes are passive and never tick: energy pushed into any face is routed
 * through the cached {@link PipeNetwork} directly to the receivers attached to the network.
 */
public abstract class BaseEnergyPipeBlockEntity extends BlockEntity {
    private static final Direction[] DIRECTIONS = Direction.values();

    protected final int transferRate;
    private final PipeFaceHandler[] faceHandlers = new PipeFaceHandler[DIRECTIONS.length];
    private final PipeFaceHandler unsidedHandler;
    @Nullable
    private PipeNetwork network;
    private int endpointFaceMask;

    public BaseEnergyPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int transferRate) {
        super(type, pos, state);
        this.transferRate = transferRate;
        for (Direction side : DIRECTIONS) {
            faceHandlers[side.ordinal()] = new PipeFaceHandler(this, side);
        }
        this.unsidedHandler = new PipeFaceHandler(this, null);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.BASIC_ENERGY_PIPE.get(),
                BaseEnergyPipeBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ADVANCED_ENERGY_PIPE.get(),
                BaseEnergyPipeBlockEntity::getEnergyHandler);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.ELITE_ENERGY_PIPE.get(),
                BaseEnergyPipeBlockEntity::getEnergyHandler);
    }

    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return side == null ? unsidedHandler : faceHandlers[side.ordinal()];
    }

    public int getTransferRate() {
        return transferRate;
    }

    @Nullable
    PipeNetwork getOrBuildNetwork() {
        if (isRemoved() || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        if (network == null || !network.isValid()) {
            network = PipeNetwork.build(serverLevel, this);
        }
        return network;
    }

    void attachNetwork(PipeNetwork network) {
        PipeNetwork previous = this.network;
        this.network = network;
        this.endpointFaceMask = 0;
        if (previous != null && previous != network) {
            previous.invalidate();
        }
    }

    void detachNetwork(PipeNetwork network) {
        if (this.network == network) {
            this.network = null;
        }
    }

    void markEndpointFace(Direction side) {
        endpointFaceMask |= 1 << side.ordinal();
    }

    /**
     * Called by the pipe block when a neighbour changes. The network only needs rebuilding when a face
     * gains or loses a block entity; every other change is picked up by the capability caches.
     */
    public void onNeighborChanged(Direction side, BlockState neighborState) {
        if (network == null) {
            return;
        }
        boolean isEndpointNow = neighborState.hasBlockEntity()
                && !(level != null && level.getBlockEntity(worldPosition.relative(side)) instanceof BaseEnergyPipeBlockEntity);
        boolean wasEndpoint = (endpointFaceMask & (1 << side.ordinal())) != 0;
        if (isEndpointNow != wasEndpoint) {
            invalidateNetwork();
        }
    }

    public void invalidateNetwork() {
        if (network != null) {
            network.invalidate();
            network = null;
        }
    }

    private void invalidateNeighborNetworks() {
        if (level == null) {
            return;
        }
        for (Direction side : DIRECTIONS) {
            BlockPos neighborPos = worldPosition.relative(side);
            if (level.isLoaded(neighborPos) && level.getBlockEntity(neighborPos) instanceof BaseEnergyPipeBlockEntity neighbor) {
                neighbor.invalidateNetwork();
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            invalidateNeighborNetworks();
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
