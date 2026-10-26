package net.rainy.sun_panels.registry;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rainy.sun_panels.SunPanels;
import net.rainy.sun_panels.block.EnergyPipeBlock;
import net.rainy.sun_panels.block.EnergyPipeBlockEntity;
import net.rainy.sun_panels.block.PipeTier;
import net.rainy.sun_panels.block.SolarPanelBlock;
import net.rainy.sun_panels.block.SolarPanelBlockEntity;
import net.rainy.sun_panels.block.SolarTier;
import net.rainy.sun_panels.menu.SolarPanelMenu;

public final class SPRegistries {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SunPanels.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SunPanels.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SunPanels.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SunPanels.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SunPanels.MOD_ID);

    public static final DeferredBlock<SolarPanelBlock> BASIC_SOLAR_PANEL = panel("basic_solar_panel", SolarTier.BASIC, 3.5F);
    public static final DeferredBlock<SolarPanelBlock> ADVANCED_SOLAR_PANEL = panel("advanced_solar_panel", SolarTier.ADVANCED, 4.0F);
    public static final DeferredBlock<SolarPanelBlock> ELITE_SOLAR_PANEL = panel("elite_solar_panel", SolarTier.ELITE, 5.0F);

    public static final DeferredBlock<EnergyPipeBlock> BASIC_ENERGY_PIPE = pipe("basic_energy_pipe", PipeTier.BASIC, MapColor.COLOR_LIGHT_GRAY, 2.0F);
    public static final DeferredBlock<EnergyPipeBlock> ADVANCED_ENERGY_PIPE = pipe("advanced_energy_pipe", PipeTier.ADVANCED, MapColor.COLOR_BLUE, 2.5F);
    public static final DeferredBlock<EnergyPipeBlock> ELITE_ENERGY_PIPE = pipe("elite_energy_pipe", PipeTier.ELITE, MapColor.COLOR_PURPLE, 3.0F);

    public static final DeferredItem<BlockItem> BASIC_SOLAR_PANEL_ITEM = ITEMS.registerSimpleBlockItem(BASIC_SOLAR_PANEL);
    public static final DeferredItem<BlockItem> ADVANCED_SOLAR_PANEL_ITEM = ITEMS.registerSimpleBlockItem(ADVANCED_SOLAR_PANEL);
    public static final DeferredItem<BlockItem> ELITE_SOLAR_PANEL_ITEM = ITEMS.registerSimpleBlockItem(ELITE_SOLAR_PANEL);
    public static final DeferredItem<BlockItem> BASIC_ENERGY_PIPE_ITEM = ITEMS.registerSimpleBlockItem(BASIC_ENERGY_PIPE);
    public static final DeferredItem<BlockItem> ADVANCED_ENERGY_PIPE_ITEM = ITEMS.registerSimpleBlockItem(ADVANCED_ENERGY_PIPE);
    public static final DeferredItem<BlockItem> ELITE_ENERGY_PIPE_ITEM = ITEMS.registerSimpleBlockItem(ELITE_ENERGY_PIPE);

    public static final Supplier<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_BE = BLOCK_ENTITIES.register("solar_panel",
            () -> new BlockEntityType<>(SolarPanelBlockEntity::new,
                    BASIC_SOLAR_PANEL.get(), ADVANCED_SOLAR_PANEL.get(), ELITE_SOLAR_PANEL.get()));

    public static final Supplier<BlockEntityType<EnergyPipeBlockEntity>> ENERGY_PIPE_BE = BLOCK_ENTITIES.register("energy_pipe",
            () -> new BlockEntityType<>(EnergyPipeBlockEntity::new,
                    BASIC_ENERGY_PIPE.get(), ADVANCED_ENERGY_PIPE.get(), ELITE_ENERGY_PIPE.get()));

    public static final Supplier<MenuType<SolarPanelMenu>> SOLAR_PANEL_MENU = MENUS.register("solar_panel",
            () -> new MenuType<>(SolarPanelMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<CreativeModeTab> TAB = TABS.register("sun_panels", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sun_panels"))
            .icon(() -> new ItemStack(BASIC_SOLAR_PANEL_ITEM.get()))
            .displayItems((parameters, output) -> {
                for (DeferredItem<BlockItem> item : List.of(BASIC_SOLAR_PANEL_ITEM, ADVANCED_SOLAR_PANEL_ITEM,
                        ELITE_SOLAR_PANEL_ITEM, BASIC_ENERGY_PIPE_ITEM, ADVANCED_ENERGY_PIPE_ITEM, ELITE_ENERGY_PIPE_ITEM)) {
                    output.accept(item.get());
                }
            })
            .build());

    private SPRegistries() {}

    private static DeferredBlock<SolarPanelBlock> panel(String name, SolarTier tier, float strength) {
        return BLOCKS.registerBlock(name, properties -> new SolarPanelBlock(tier, properties), properties -> properties
                .mapColor(MapColor.METAL)
                .strength(strength, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }

    private static DeferredBlock<EnergyPipeBlock> pipe(String name, PipeTier tier, MapColor color, float strength) {
        return BLOCKS.registerBlock(name, properties -> new EnergyPipeBlock(tier, properties), properties -> properties
                .mapColor(color)
                .strength(strength)
                .sound(SoundType.METAL)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        TABS.register(modBus);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, SOLAR_PANEL_BE.get(),
                (panel, side) -> panel.getExposedEnergy());
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ENERGY_PIPE_BE.get(),
                EnergyPipeBlockEntity::getEnergyHandler);
    }
}
