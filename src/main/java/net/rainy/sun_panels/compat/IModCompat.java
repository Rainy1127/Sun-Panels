package net.rainy.sun_panels.compat;

import net.neoforged.bus.api.IEventBus;

/**
 * Interface for mod compatibility handlers.
 * Any class that provides compatibility with another mod should implement this interface.
 */
public interface IModCompat {

    /**
     * Initializes the compatibility handler.
     * @param eventBus The mod event bus for registering events
     */
    void initialize(IEventBus eventBus);

    /**
     * Checks if this compatibility handler is enabled.
     * @return True if the handler is enabled
     */
    boolean isEnabled();
}