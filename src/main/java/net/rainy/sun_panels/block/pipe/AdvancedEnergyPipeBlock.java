package net.rainy.sun_panels.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.entity.pipe.AdvancedEnergyPipeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Advanced energy pipe with medium transfer rate
 */
public class AdvancedEnergyPipeBlock extends BaseEnergyPipeBlock {
    // Transfer rate in RF/t for advanced pipe
    public static final int ADVANCED_TRANSFER_RATE = 500;

    public AdvancedEnergyPipeBlock(Properties properties) {
        super(properties, ADVANCED_TRANSFER_RATE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AdvancedEnergyPipeBlockEntity(pos, state);
    }


}