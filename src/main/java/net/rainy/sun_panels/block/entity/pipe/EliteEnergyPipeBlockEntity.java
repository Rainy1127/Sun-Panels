package net.rainy.sun_panels.block.entity.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.pipe.EliteEnergyPipeBlock;
import net.rainy.sun_panels.registry.ModBlockEntities;

/**
 * Block entity for Elite Energy Pipe
 */
public class EliteEnergyPipeBlockEntity extends BaseEnergyPipeBlockEntity {
    public EliteEnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELITE_ENERGY_PIPE.get(), pos, state, EliteEnergyPipeBlock.ELITE_TRANSFER_RATE);
    }
}