package net.rainy.sun_panels.block.entity.pipe;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * The energy capability a pipe exposes on one face. It only accepts energy and forwards it
 * through the pipe network immediately; it never stores or hands out energy itself.
 */
public final class PipeFaceHandler implements IEnergyStorage {
    private final BaseEnergyPipeBlockEntity pipe;
    @Nullable
    private final Direction side;

    PipeFaceHandler(BaseEnergyPipeBlockEntity pipe, @Nullable Direction side) {
        this.pipe = pipe;
        this.side = side;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        PipeNetwork network = pipe.getOrBuildNetwork();
        if (network == null) {
            return 0;
        }
        return network.distribute(maxReceive, pipe.getBlockPos(), side, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return pipe.getTransferRate();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
