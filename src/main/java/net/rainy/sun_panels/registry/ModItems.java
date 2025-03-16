package net.rainy.sun_panels.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rainy.sun_panels.Sun_Panels;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Sun_Panels.MOD_ID);

    // Solar Panel Block Items
    public static final DeferredItem<Item> BASIC_SOLAR_PANEL_ITEM = ITEMS.register("basic_solar_panel",
            () -> new BlockItem(ModBlocks.BASIC_SOLAR_PANEL.get(), new Item.Properties()));

    public static final DeferredItem<Item> ADVANCED_SOLAR_PANEL_ITEM = ITEMS.register("advanced_solar_panel",
            () -> new BlockItem(ModBlocks.ADVANCED_SOLAR_PANEL.get(), new Item.Properties()));

    public static final DeferredItem<Item> ELITE_SOLAR_PANEL_ITEM = ITEMS.register("elite_solar_panel",
            () -> new BlockItem(ModBlocks.ELITE_SOLAR_PANEL.get(), new Item.Properties()));

    // Energy Pipe Block Items
    public static final DeferredItem<Item> BASIC_ENERGY_PIPE_ITEM = ITEMS.register("basic_energy_pipe",
            () -> new BlockItem(ModBlocks.BASIC_ENERGY_PIPE.get(), new Item.Properties()));

    public static final DeferredItem<Item> ADVANCED_ENERGY_PIPE_ITEM = ITEMS.register("advanced_energy_pipe",
            () -> new BlockItem(ModBlocks.ADVANCED_ENERGY_PIPE.get(), new Item.Properties()));

    public static final DeferredItem<Item> ELITE_ENERGY_PIPE_ITEM = ITEMS.register("elite_energy_pipe",
            () -> new BlockItem(ModBlocks.ELITE_ENERGY_PIPE.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}