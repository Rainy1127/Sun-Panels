package net.rainy.sun_panels.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.entity.pipe.AdvancedEnergyPipeBlockEntity;
import net.rainy.sun_panels.registry.ModBlockEntities;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.ADVANCED_ENERGY_PIPE.get(), AdvancedEnergyPipeBlockEntity::serverTick);
    }

    @Nullable
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeCheck, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == typeCheck ? (BlockEntityTicker<A>) ticker : null;
    }
}