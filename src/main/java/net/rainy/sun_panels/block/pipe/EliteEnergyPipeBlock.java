package net.rainy.sun_panels.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.entity.pipe.EliteEnergyPipeBlockEntity;
import net.rainy.sun_panels.registry.ModBlockEntities;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.ELITE_ENERGY_PIPE.get(), EliteEnergyPipeBlockEntity::serverTick);
    }

    @Nullable
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeCheck, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == typeCheck ? (BlockEntityTicker<A>) ticker : null;
    }
}