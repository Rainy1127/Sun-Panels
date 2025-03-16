package net.rainy.sun_panels.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.rainy.sun_panels.menu.SolarPanelMenu;
import net.rainy.sun_panels.registry.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicSolarPanelBlockEntity extends BaseSolarPanelBlockEntity implements MenuProvider {
    // Basic Solar Panel: 32 RF/t, 4,000 RF storage
    private static final int CAPACITY = 4000;
    private static final int GENERATION_RATE = 32;
    private static final int MAX_GENERATION = GENERATION_RATE; // Maximum generation rate

    public BasicSolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_SOLAR_PANEL.get(), pos, state, CAPACITY, GENERATION_RATE);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BasicSolarPanelBlockEntity blockEntity) {
        BaseSolarPanelBlockEntity.tick(level, pos, state, blockEntity);
    }

    public int getMaxGeneration() {
        return MAX_GENERATION;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.rainy.basic_solar_panel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SolarPanelMenu(containerId, inventory, this,
                net.minecraft.world.inventory.ContainerLevelAccess.create(level, worldPosition));
    }
}