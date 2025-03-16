package net.rainy.sun_panels.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.entity.pipe.BasicEnergyPipeBlockEntity;
import net.rainy.sun_panels.registry.ModBlockEntities;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.BASIC_ENERGY_PIPE.get(), BasicEnergyPipeBlockEntity::serverTick);
    }

    @Nullable
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeCheck, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == typeCheck ? (BlockEntityTicker<A>) ticker : null;
    }
}