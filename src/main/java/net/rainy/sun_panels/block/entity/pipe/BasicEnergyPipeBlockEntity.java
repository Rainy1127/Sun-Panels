package net.rainy.sun_panels.block.entity.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.pipe.BasicEnergyPipeBlock;
import net.rainy.sun_panels.registry.ModBlockEntities;


/**
 * Block entity for Basic Energy Pipe
 */
public class BasicEnergyPipeBlockEntity extends BaseEnergyPipeBlockEntity {
    public BasicEnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_ENERGY_PIPE.get(), pos, state, BasicEnergyPipeBlock.BASIC_TRANSFER_RATE);
    }
}