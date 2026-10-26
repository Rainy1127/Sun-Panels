package net.rainy.sun_panels.energy;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * What other blocks see on a solar panel: energy can be taken out, never pushed in.
 * This keeps pipes and machines from bouncing energy back into panels.
 */
public final class ExtractOnlyEnergyHandler implements EnergyHandler {
    private final EnergyHandler delegate;

    public ExtractOnlyEnergyHandler(EnergyHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public long getAmountAsLong() {
        return delegate.getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return delegate.getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return delegate.extract(amount, transaction);
    }
}
