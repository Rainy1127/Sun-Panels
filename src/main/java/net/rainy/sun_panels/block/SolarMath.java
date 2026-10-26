package net.rainy.sun_panels.block;

/**
 * Pure solar output math, kept free of Minecraft classes so it can be unit-tested.
 *
 * <p>Input is the effective sky brightness of the space above the panel (sky light minus the current
 * darkening from time of day and weather, 0..15). At night vanilla darkening is 11, so the effective
 * value bottoms out at 4: everything at or below {@link #NIGHT_LEVEL} produces nothing, and output rises
 * linearly to 100% at full clear daylight. Dawn, dusk, rain and thunder therefore scale smoothly.
 */
public final class SolarMath {
    public static final int NIGHT_LEVEL = 4;
    public static final int MAX_LEVEL = 15;

    private SolarMath() {}

    public static int output(int maxGeneration, int effectiveSkyBrightness) {
        int level = Math.min(MAX_LEVEL, effectiveSkyBrightness);
        if (level <= NIGHT_LEVEL || maxGeneration <= 0) {
            return 0;
        }
        return Math.max(1, Math.round(maxGeneration * (float) (level - NIGHT_LEVEL) / (MAX_LEVEL - NIGHT_LEVEL)));
    }
}
