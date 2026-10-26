package net.rainy.sun_panels;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.rainy.sun_panels.registry.SPRegistries;

@Mod(SunPanels.MOD_ID)
public final class SunPanels {
    public static final String MOD_ID = "sun_panels";

    public SunPanels(IEventBus modBus, ModContainer container) {
        SPRegistries.register(modBus);
        modBus.addListener(SPRegistries::registerCapabilities);
    }
}
