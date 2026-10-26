package net.rainy.sun_panels.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.registry.ModBlockEntities;

public class EliteSolarPanelBlockEntity extends BaseSolarPanelBlockEntity {
    // Elite Solar Panel: 512 RF/t, 64,000 RF storage
    private static final int CAPACITY = 64000;
    private static final int GENERATION_RATE = 512;

    public EliteSolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELITE_SOLAR_PANEL.get(), pos, state, CAPACITY, GENERATION_RATE);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EliteSolarPanelBlockEntity blockEntity) {
        BaseSolarPanelBlockEntity.tick(level, pos, state, blockEntity);
    }

    public int getMaxGeneration() {
        return GENERATION_RATE;
    }
}
