package net.rainy.sun_panels.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.rainy.sun_panels.registry.SPRegistries;

public class SolarPanelBlock extends Block implements EntityBlock {
    public static final MapCodec<SolarPanelBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SolarTier.CODEC.fieldOf("tier").forGetter(SolarPanelBlock::tier),
            propertiesCodec()
    ).apply(instance, SolarPanelBlock::new));

    private final SolarTier tier;

    public SolarPanelBlock(SolarTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public SolarTier tier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    /** Panels are the only ticking block of the mod: generation has to follow the sun every tick. */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != SPRegistries.SOLAR_PANEL_BE.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<SolarPanelBlockEntity>) SolarPanelBlockEntity::serverTick;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) {
                player.openMenu(provider);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SolarPanelBlockEntity panel ? panel : null;
    }
}
