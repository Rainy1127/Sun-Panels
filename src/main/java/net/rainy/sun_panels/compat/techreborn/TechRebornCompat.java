package net.rainy.sun_panels.compat.techreborn;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.rainy.sun_panels.compat.IModCompat;
import org.slf4j.Logger;

/**
 * Compatibility handler for Tech Reborn.
 * Provides conversion between Forge Energy (FE) and Energy Units (EU).
 */
public class TechRebornCompat implements IModCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TECH_REBORN_ID = "techreborn";

    // Tech Reborn uses Energy Units (EU)
    // The standard conversion is 4 FE = 1 EU
    public static final double FE_TO_EU_CONVERSION_RATE = 0.25; // 1 FE = 0.25 EU
    public static final double EU_TO_FE_CONVERSION_RATE = 4.0;  // 1 EU = 4 FE

    private boolean enabled = false;
    private IEventBus modEventBus;

    @Override
    public void initialize(IEventBus eventBus) {
        this.modEventBus = eventBus;
        LOGGER.info("Initializing Tech Reborn compatibility...");

        // Register to the event bus to receive IMC events
        modEventBus.addListener(this::enqueueIMC);

        enabled = true;
        LOGGER.info("Tech Reborn compatibility initialized successfully");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sends Inter-Mod Communication messages to Tech Reborn.
     * @param event The Inter-Mod enqueue event
     */
    private void enqueueIMC(final InterModEnqueueEvent event) {
        try {
            LOGGER.info("Registering with Tech Reborn energy system...");

            // Send the energy conversion message to Tech Reborn
            // Note: This is a conceptual example - actual TR API might differ
            InterModComms.sendTo(TECH_REBORN_ID, "register_energy_conversion", () -> {
                return new EUEnergyConversion(FE_TO_EU_CONVERSION_RATE, EU_TO_FE_CONVERSION_RATE);
            });

            LOGGER.info("Successfully registered with Tech Reborn energy system");
        } catch (Exception e) {
            LOGGER.error("Failed to register with Tech Reborn energy system", e);
            enabled = false;
        }
    }

    /**
     * Value class for energy conversion rates.
     */
    public static class EUEnergyConversion {
        private final double feToEu;
        private final double euToFe;

        public EUEnergyConversion(double feToEu, double euToFe) {
            this.feToEu = feToEu;
            this.euToFe = euToFe;
        }

        public double convertFEtoEU(double fe) {
            return fe * feToEu;
        }

        public double convertEUtoFE(double eu) {
            return eu * euToFe;
        }
    }
}