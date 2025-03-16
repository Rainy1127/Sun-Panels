package net.rainy.sun_panels;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.rainy.sun_panels.block.entity.BaseSolarPanelBlockEntity;
import net.rainy.sun_panels.compat.ModCompatManager;
import net.rainy.sun_panels.registry.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Sun_Panels.MOD_ID)
public class Sun_Panels
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "sun_panels";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Sun_Panels(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Sun Panels Mod");

        // Register all of our mod components
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // Initialize mod compatibility
        ModCompatManager.initialize(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the capabilities
        modEventBus.addListener(this::registerCapabilities);

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Sun Panels common setup");
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        BaseSolarPanelBlockEntity.registerCapabilities(event);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTab.SUN_PANELS_TAB.getKey()) {
            event.accept(ModItems.BASIC_SOLAR_PANEL_ITEM);
            event.accept(ModItems.ADVANCED_SOLAR_PANEL_ITEM);
            event.accept(ModItems.ELITE_SOLAR_PANEL_ITEM);

            event.accept(ModItems.BASIC_ENERGY_PIPE_ITEM);
            event.accept(ModItems.ADVANCED_ENERGY_PIPE_ITEM);
            event.accept(ModItems.ELITE_ENERGY_PIPE_ITEM);
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.BASIC_SOLAR_PANEL_ITEM);
            event.accept(ModItems.ADVANCED_SOLAR_PANEL_ITEM);
            event.accept(ModItems.ELITE_SOLAR_PANEL_ITEM);

            event.accept(ModItems.BASIC_ENERGY_PIPE_ITEM);
            event.accept(ModItems.ADVANCED_ENERGY_PIPE_ITEM);
            event.accept(ModItems.ELITE_ENERGY_PIPE_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Sun Panels loaded on server");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Sun Panels client setup");
        }
    }
}
