package net.rainy.sun_panels.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.entity.pipe.BasicEnergyPipeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Basic energy pipe with lowest transfer rate
 */
public class BasicEnergyPipeBlock extends BaseEnergyPipeBlock {
    // Transfer rate in RF/t for basic pipe
    public static final int BASIC_TRANSFER_RATE = 100;

    public BasicEnergyPipeBlock(Properties properties) {
        super(properties, BASIC_TRANSFER_RATE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new BasicEnergyPipeBlockEntity(pos, state);
    }


}