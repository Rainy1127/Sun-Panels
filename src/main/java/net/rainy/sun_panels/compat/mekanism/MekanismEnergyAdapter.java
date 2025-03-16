package net.rainy.sun_panels.compat.mekanism;

import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter class to make our energy system compatible with Mekanism's energy API.
 */
public class MekanismEnergyAdapter {
    // The standard conversion rate from Forge Energy to Mekanism Joules
    public static final double CONVERSION_RATE = 2.5;

    private final IEnergyStorage energyStorage;

    public MekanismEnergyAdapter(@NotNull IEnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    /**
     * Gets the amount of energy stored in this component as Mekanism Joules.
     * @return Amount of energy stored in Joules
     */
    public double getEnergy() {
        return energyStorage.getEnergyStored() * CONVERSION_RATE;
    }

    /**
     * Gets the maximum amount of energy that can be stored in this component as Mekanism Joules.
     * @return Maximum energy storage in Joules
     */
    public double getMaxEnergy() {
        return energyStorage.getMaxEnergyStored() * CONVERSION_RATE;
    }

    /**
     * Adds the specified amount of energy to this component's storage.
     * @param amount Amount of energy in Joules to add
     * @return Amount of energy that was added in Joules
     */
    public double addEnergy(double amount) {
        int feAmount = (int) Math.floor(amount / CONVERSION_RATE);
        int accepted = energyStorage.receiveEnergy(feAmount, false);
        return accepted * CONVERSION_RATE;
    }

    /**
     * Removes the specified amount of energy from this component's storage.
     * @param amount Amount of energy in Joules to remove
     * @return True if the energy was successfully removed
     */
    public boolean removeEnergy(double amount) {
        int feAmount = (int) Math.ceil(amount / CONVERSION_RATE);
        int extracted = energyStorage.extractEnergy(feAmount, false);
        return extracted > 0;
    }

    /**
     * Checks if this energy storage can have energy extracted.
     * @return True if energy extraction is allowed
     */
    public boolean canExtract() {
        return energyStorage.canExtract();
    }

    /**
     * Checks if this energy storage can receive energy.
     * @return True if energy insertion is allowed
     */
    public boolean canInsert() {
        return energyStorage.canReceive();
    }
}