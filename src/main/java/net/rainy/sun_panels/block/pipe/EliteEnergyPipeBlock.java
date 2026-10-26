package net.rainy.sun_panels.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.entity.pipe.EliteEnergyPipeBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Elite energy pipe with highest transfer rate
 */
public class EliteEnergyPipeBlock extends BaseEnergyPipeBlock {
    // Transfer rate in RF/t for elite pipe
    public static final int ELITE_TRANSFER_RATE = 2000;

    public EliteEnergyPipeBlock(Properties properties) {
        super(properties, ELITE_TRANSFER_RATE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new EliteEnergyPipeBlockEntity(pos, state);
    }


}