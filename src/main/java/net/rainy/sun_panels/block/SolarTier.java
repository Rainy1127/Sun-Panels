package net.rainy.sun_panels.block;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Panel tiers. Generation is the output at full, clear daylight; capacity is the internal buffer. */
public enum SolarTier implements StringRepresentable {
    BASIC("basic", 32, 4_000),
    ADVANCED("advanced", 128, 16_000),
    ELITE("elite", 512, 64_000);

    public static final Codec<SolarTier> CODEC = StringRepresentable.fromEnum(SolarTier::values);

    private final String name;
    private final int generation;
    private final int capacity;

    SolarTier(String name, int generation, int capacity) {
        this.name = name;
        this.generation = generation;
        this.capacity = capacity;
    }

    public int generation() {
        return generation;
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
