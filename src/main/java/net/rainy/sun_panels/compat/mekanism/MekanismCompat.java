package net.rainy.sun_panels.compat.mekanism;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.rainy.sun_panels.compat.IModCompat;
import org.slf4j.Logger;

/**
 * Handles compatibility with the Mekanism mod.
 * This class is designed to safely initialize even if Mekanism is not present.
 */
public class MekanismCompat implements IModCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MEKANISM_MODID = "mekanism";
    private static final String ENERGY_COMPAT_METHOD = "api_energy";
    private IEventBus modEventBus;
    private boolean enabled = false;

    /**
     * Initializes Mekanism compatibility if the mod is present.
     * Safe to call even if Mekanism is not installed.
     */
    @Override
    public void initialize(IEventBus eventBus) {
        this.modEventBus = eventBus;
        boolean mekanismLoaded = ModList.get().isLoaded(MEKANISM_MODID);

        if (mekanismLoaded) {
            LOGGER.info("Mekanism detected, enabling energy compatibility...");

            // Register to the mod event bus to receive IMC events
            modEventBus.addListener(this::enqueueIMC);
            enabled = true;
        } else {
            LOGGER.info("Mekanism not detected, skipping compatibility integration");
            enabled = false;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sends an Inter-Mod Communication (IMC) message to Mekanism.
     * This registers our energy compatibility handler.
     *
     * @param event The Inter-Mod enqueue event
     */
    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (!enabled) {
            return;
        }

        try {
            LOGGER.info("Registering energy handler with Mekanism...");

            // Create a supplier for our energy compatibility handler
            InterModComms.sendTo(MEKANISM_MODID, ENERGY_COMPAT_METHOD, SolarPanelEnergyCompat::new);

            LOGGER.info("Successfully registered energy handler with Mekanism");
        } catch (Exception e) {
            LOGGER.error("Failed to register energy handler with Mekanism", e);
            enabled = false;
        }
    }

    /**
     * Checks if Mekanism is loaded and compatibility is enabled.
     *
     * @return true if Mekanism compatibility is enabled, false otherwise
     */
    public static boolean isMekanismAvailable() {
        return ModList.get().isLoaded(MEKANISM_MODID);
    }
}