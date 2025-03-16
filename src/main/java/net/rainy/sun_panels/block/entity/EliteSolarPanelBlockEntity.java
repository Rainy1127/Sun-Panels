package net.rainy.sun_panels.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.menu.SolarPanelMenu;
import net.rainy.sun_panels.registry.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EliteSolarPanelBlockEntity extends BaseSolarPanelBlockEntity implements MenuProvider {
    // Elite Solar Panel: 512 RF/t, 64,000 RF storage
    private static final int CAPACITY = 64000;
    private static final int GENERATION_RATE = 512;
    private static final int MAX_GENERATION = GENERATION_RATE; // Maximum generation rate

    public EliteSolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELITE_SOLAR_PANEL.get(), pos, state, CAPACITY, GENERATION_RATE);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EliteSolarPanelBlockEntity blockEntity) {
        BaseSolarPanelBlockEntity.tick(level, pos, state, blockEntity);
    }

    public int getMaxGeneration() {
        return MAX_GENERATION;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.rainy.elite_solar_panel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SolarPanelMenu(containerId, inventory, this,
                net.minecraft.world.inventory.ContainerLevelAccess.create(level, worldPosition));
    }
}