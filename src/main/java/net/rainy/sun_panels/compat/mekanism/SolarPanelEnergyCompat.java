package net.rainy.sun_panels.compat.mekanism;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.rainy.sun_panels.block.entity.BaseSolarPanelBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation class for Mekanism's energy API.
 * This class will be registered via IMC to allow Mekanism to interact with our energy storage.
 */
public class SolarPanelEnergyCompat {

    /**
     * Checks if a block entity at the given position can be handled by this energy handler.
     *
     * @param level The level containing the block entity
     * @param pos The position of the block entity
     * @param side The side being accessed
     * @return True if the block entity can be handled by this energy handler
     */
    public boolean canHandle(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof BaseSolarPanelBlockEntity;
    }

    /**
     * Gets the energy stored in the block entity as Mekanism Joules.
     *
     * @param level The level containing the block entity
     * @param pos The position of the block entity
     * @param side The side being accessed
     * @return The amount of energy stored in Joules
     */
    public double getEnergy(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseSolarPanelBlockEntity solarPanel) {
            return solarPanel.getMekanismEnergyAdapter().getEnergy();
        }
        return 0;
    }

    /**
     * Gets the maximum energy capacity of the block entity as Mekanism Joules.
     *
     * @param level The level containing the block entity
     * @param pos The position of the block entity
     * @param side The side being accessed
     * @return The maximum energy capacity in Joules
     */
    public double getMaxEnergy(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseSolarPanelBlockEntity solarPanel) {
            return solarPanel.getMekanismEnergyAdapter().getMaxEnergy();
        }
        return 0;
    }

    /**
     * Extracts energy from the block entity.
     *
     * @param level The level containing the block entity
     * @param pos The position of the block entity
     * @param side The side being accessed
     * @param amount The amount of energy to extract in Joules
     * @return True if energy was successfully extracted
     */
    public boolean extractEnergy(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side, double amount) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseSolarPanelBlockEntity solarPanel) {
            return solarPanel.getMekanismEnergyAdapter().removeEnergy(amount);
        }
        return false;
    }

    /**
     * Inserts energy into the block entity.
     *
     * @param level The level containing the block entity
     * @param pos The position of the block entity
     * @param side The side being accessed
     * @param amount The amount of energy to insert in Joules
     * @return The amount of energy that was inserted in Joules
     */
    public double insertEnergy(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side, double amount) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseSolarPanelBlockEntity solarPanel) {
            return solarPanel.getMekanismEnergyAdapter().addEnergy(amount);
        }
        return 0;
    }

    /**
     * Checks if energy can be extracted from the block entity.
     *
     * @param level The level containing the block entity
     * @param pos The position of the block entity
     * @param side The side being accessed
     * @return True if energy can be extracted
     */
    public boolean canExtract(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseSolarPanelBlockEntity solarPanel) {
            return solarPanel.getMekanismEnergyAdapter().canExtract();
        }
        return false;
    }

    /**
     * Checks if energy can be inserted into the block entity.
     *
     * @param level The level containing the block entity
     * @param pos The position of the block entity
     * @param side The side being accessed
     * @return True if energy can be inserted
     */
    public boolean canInsert(@NotNull Level level, @NotNull BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseSolarPanelBlockEntity solarPanel) {
            return solarPanel.getMekanismEnergyAdapter().canInsert();
        }
        return false;
    }
}