package net.rainy.sun_panels.energy;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.rainy.sun_panels.block.EnergyPipeBlockEntity;

/**
 * The energy capability of one pipe face. It stores nothing: inserted energy is handed straight to the
 * receivers of the pipe network inside the caller's transaction, so a rollback undoes everything.
 */
public final class PipeFaceHandler implements EnergyHandler {
    private final EnergyPipeBlockEntity pipe;
    private final Direction side;

    public PipeFaceHandler(EnergyPipeBlockEntity pipe, Direction side) {
        this.pipe = pipe;
        this.side = side;
    }

    @Override
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return pipe.getTier().transferRate();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        PipeNetwork network = pipe.getOrBuildNetwork();
        return network == null ? 0 : network.distribute(amount, pipe.getBlockPos(), side, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}
