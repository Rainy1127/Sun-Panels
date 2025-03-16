package net.rainy.sun_panels.compat;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.rainy.sun_panels.compat.mekanism.MekanismCompat;
import net.rainy.sun_panels.compat.techreborn.TechRebornCompat;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Central manager for mod compatibility with various energy systems.
 * This class checks for and initializes compatibility with various energy mods.
 */
public class ModCompatManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, IModCompat> compatHandlers = new HashMap<>();

    // Add supported mods here with their mod IDs
    public static final String MEKANISM_ID = "mekanism";
    public static final String TECH_REBORN_ID = "techreborn";
    public static final String THERMAL_ID = "thermal";
    public static final String RFTOOLS_ID = "rftoolspower";
    public static final String POWAH_ID = "powah";
    public static final String ENDER_IO_ID = "enderio";
    public static final String AE2_ID = "ae2";
    public static final String IMMERSIVE_ENGINEERING_ID = "immersiveengineering";

    /**
     * Initializes compatibility with all supported mods.
     * @param eventBus The mod event bus for registering events
     */
    public static void initialize(IEventBus eventBus) {
        LOGGER.info("Initializing mod compatibility systems...");

        // Register all compatibility handlers
        registerCompatHandler(MEKANISM_ID, new MekanismCompat());
        registerCompatHandler(TECH_REBORN_ID, new TechRebornCompat());

        // Initialize all registered handlers
        int loadedModsCount = 0;
        for (Map.Entry<String, IModCompat> entry : compatHandlers.entrySet()) {
            String modId = entry.getKey();
            IModCompat handler = entry.getValue();

            if (ModList.get().isLoaded(modId)) {
                LOGGER.info("Found compatible mod: {}. Enabling integration.", modId);
                handler.initialize(eventBus);
                loadedModsCount++;
            }
        }

        // Log native compatibility with FE-based mods (no adapter needed)
        String[] nativeCompatMods = {THERMAL_ID, RFTOOLS_ID, POWAH_ID, ENDER_IO_ID, AE2_ID, IMMERSIVE_ENGINEERING_ID};
        for (String modId : nativeCompatMods) {
            if (ModList.get().isLoaded(modId)) {
                LOGGER.info("Found compatible mod: {}. Native compatibility through Forge Energy.", modId);
                loadedModsCount++;
            }
        }

        LOGGER.info("Mod compatibility initialization complete. Found {} compatible energy mods.", loadedModsCount);
    }

    /**
     * Registers a compatibility handler for a specific mod.
     * @param modId The mod ID to register the handler for
     * @param handler The compatibility handler
     */
    private static void registerCompatHandler(String modId, IModCompat handler) {
        compatHandlers.put(modId, handler);
    }

    /**
     * Checks if a specific mod is loaded.
     * @param modId The mod ID to check
     * @return True if the mod is loaded
     */
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * Gets the compatibility handler for a specific mod.
     * @param modId The mod ID to get the handler for
     * @return The compatibility handler, or null if none exists
     */
    public static IModCompat getCompatHandler(String modId) {
        return compatHandlers.get(modId);
    }
}