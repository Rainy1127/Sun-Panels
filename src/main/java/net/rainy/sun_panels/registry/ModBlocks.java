package net.rainy.sun_panels.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rainy.sun_panels.Sun_Panels;
import net.rainy.sun_panels.block.AdvancedSolarPanelBlock;
import net.rainy.sun_panels.block.BasicSolarPanelBlock;
import net.rainy.sun_panels.block.EliteSolarPanelBlock;
import net.rainy.sun_panels.block.pipe.AdvancedEnergyPipeBlock;
import net.rainy.sun_panels.block.pipe.BasicEnergyPipeBlock;
import net.rainy.sun_panels.block.pipe.EliteEnergyPipeBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Sun_Panels.MOD_ID);

    // Solar Panel Blocks
    public static final DeferredBlock<Block> BASIC_SOLAR_PANEL = BLOCKS.register("basic_solar_panel",
            () -> new BasicSolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ADVANCED_SOLAR_PANEL = BLOCKS.register("advanced_solar_panel",
            () -> new AdvancedSolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ELITE_SOLAR_PANEL = BLOCKS.register("elite_solar_panel",
            () -> new EliteSolarPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    // Energy Pipe Blocks
    public static final DeferredBlock<Block> BASIC_ENERGY_PIPE = BLOCKS.register("basic_energy_pipe",
            () -> new BasicEnergyPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .strength(2.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion() // Important for the pipe model
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ADVANCED_ENERGY_PIPE = BLOCKS.register("advanced_energy_pipe",
            () -> new AdvancedEnergyPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion() // Important for the pipe model
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> ELITE_ENERGY_PIPE = BLOCKS.register("elite_energy_pipe",
            () -> new EliteEnergyPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion() // Important for the pipe model
                    .requiresCorrectToolForDrops()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}