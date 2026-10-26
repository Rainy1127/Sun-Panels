package net.rainy.sun_panels.energy;

import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

/** The panel's own buffer. Generation is added directly; extraction goes through transactions. */
public class PanelEnergyHandler extends SimpleEnergyHandler {
    private final Runnable onChanged;

    public PanelEnergyHandler(int capacity, Runnable onChanged) {
        super(capacity, capacity, capacity);
        this.onChanged = onChanged;
    }

    /** Adds generated energy outside of any transaction, clamped to the capacity. */
    public void generate(int amount) {
        if (amount > 0 && energy < capacity) {
            set(Math.min(capacity, energy + amount));
        }
    }

    public int getEnergy() {
        return energy;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        onChanged.run();
    }
}
