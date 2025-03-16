package net.rainy.sun_panels.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.rainy.sun_panels.Sun_Panels;
import net.rainy.sun_panels.client.gui.SolarPanelScreen;
import net.rainy.sun_panels.registry.ModMenuTypes;

@EventBusSubscriber(modid = Sun_Panels.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client setup code here
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SOLAR_PANEL_MENU.get(), SolarPanelScreen::new);
    }
}