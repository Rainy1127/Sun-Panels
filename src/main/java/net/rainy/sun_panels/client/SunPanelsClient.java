package net.rainy.sun_panels.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.rainy.sun_panels.SunPanels;
import net.rainy.sun_panels.registry.SPRegistries;

/** Client-only entry point; nothing in this package is loaded on a dedicated server. */
@Mod(value = SunPanels.MOD_ID, dist = Dist.CLIENT)
public final class SunPanelsClient {
    public SunPanelsClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(SunPanelsClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SPRegistries.SOLAR_PANEL_MENU.get(), SolarPanelScreen::new);
    }
}
