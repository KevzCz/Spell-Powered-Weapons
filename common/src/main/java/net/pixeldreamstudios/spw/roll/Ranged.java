package net.pixeldreamstudios.spw.roll;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public record Ranged(float min, float max) {

    private static final Codec<Ranged> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("min").forGetter(Ranged::min),
            Codec.FLOAT.fieldOf("max").forGetter(Ranged::max)
    ).apply(instance, Ranged::new));

    private static final Codec<Ranged> FIXED_CODEC = Codec.FLOAT.xmap(
            value -> new Ranged(value, value),
            ranged -> ranged.min == ranged.max ? ranged.min : ranged.max);

    public static final Codec<Ranged> CODEC = Codec.either(OBJECT_CODEC, FIXED_CODEC)
            .xmap(either -> either.map(value -> value, value -> value), Either::left);

    public static final Ranged ZERO = new Ranged(0f, 0f);

    public float sample(RandomSource random) {
        float lo = Math.min(min, max);
        float hi = Math.max(min, max);
        return lo >= hi ? lo : lo + random.nextFloat() * (hi - lo);
    }
}
