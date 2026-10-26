package net.rainy.sun_panels.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.registry.ModBlockEntities;

public class BasicSolarPanelBlockEntity extends BaseSolarPanelBlockEntity {
    // Basic Solar Panel: 32 RF/t, 4,000 RF storage
    private static final int CAPACITY = 4000;
    private static final int GENERATION_RATE = 32;

    public BasicSolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_SOLAR_PANEL.get(), pos, state, CAPACITY, GENERATION_RATE);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BasicSolarPanelBlockEntity blockEntity) {
        BaseSolarPanelBlockEntity.tick(level, pos, state, blockEntity);
    }

    public int getMaxGeneration() {
        return GENERATION_RATE;
    }
}
