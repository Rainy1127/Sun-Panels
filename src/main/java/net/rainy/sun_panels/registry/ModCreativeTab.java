package net.rainy.sun_panels.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rainy.sun_panels.Sun_Panels;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Sun_Panels.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SUN_PANELS_TAB =
            CREATIVE_TABS.register("sun_panels_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + Sun_Panels.MOD_ID + ".sun_panels_tab"))
                            .icon(() -> new ItemStack(ModItems.BASIC_SOLAR_PANEL_ITEM.get()))
                            .displayItems((parameters, output) -> {
                                // Items will be added via the BuildCreativeModeTabContentsEvent
                            })
                            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}