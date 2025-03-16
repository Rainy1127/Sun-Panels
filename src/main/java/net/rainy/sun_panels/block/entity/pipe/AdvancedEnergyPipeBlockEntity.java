package net.rainy.sun_panels.block.entity.pipe;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.rainy.sun_panels.block.pipe.AdvancedEnergyPipeBlock;
import net.rainy.sun_panels.registry.ModBlockEntities;

/**
 * Block entity for Advanced Energy Pipe
 */
public class AdvancedEnergyPipeBlockEntity extends BaseEnergyPipeBlockEntity {
    public AdvancedEnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_ENERGY_PIPE.get(), pos, state, AdvancedEnergyPipeBlock.ADVANCED_TRANSFER_RATE);
    }
}