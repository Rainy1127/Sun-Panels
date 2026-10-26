package net.rainy.sun_panels.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.registry.ModBlockEntities;

public class AdvancedSolarPanelBlockEntity extends BaseSolarPanelBlockEntity {
    // Advanced Solar Panel: 128 RF/t, 16,000 RF storage
    private static final int CAPACITY = 16000;
    private static final int GENERATION_RATE = 128;

    public AdvancedSolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_SOLAR_PANEL.get(), pos, state, CAPACITY, GENERATION_RATE);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedSolarPanelBlockEntity blockEntity) {
        BaseSolarPanelBlockEntity.tick(level, pos, state, blockEntity);
    }

    public int getMaxGeneration() {
        return GENERATION_RATE;
    }
}
