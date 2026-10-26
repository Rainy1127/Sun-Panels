package net.rainy.sun_panels.block;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Pipe tiers. A pipe network moves at most the rate of its slowest pipe per tick. */
public enum PipeTier implements StringRepresentable {
    BASIC("basic", 100),
    ADVANCED("advanced", 500),
    ELITE("elite", 2_000);

    public static final Codec<PipeTier> CODEC = StringRepresentable.fromEnum(PipeTier::values);

    private final String name;
    private final int transferRate;

    PipeTier(String name, int transferRate) {
        this.name = name;
        this.transferRate = transferRate;
    }

    public int transferRate() {
        return transferRate;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
