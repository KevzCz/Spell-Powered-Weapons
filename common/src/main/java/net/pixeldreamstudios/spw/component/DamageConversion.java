package net.pixeldreamstudios.spw.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record DamageConversion(List<Entry> entries) {

    public static final DamageConversion EMPTY = new DamageConversion(List.of());

    public static final Codec<DamageConversion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(DamageConversion::entries)
    ).apply(instance, DamageConversion::new));

    public static final StreamCodec<ByteBuf, DamageConversion> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC);

    public enum Mode {
        SPLIT,
        ADDITIVE,
        PROPORTIONAL;

        public static final Codec<Mode> CODEC = Codec.STRING.xmap(
                raw -> {
                    try {
                        return valueOf(raw.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ignored) {
                        return SPLIT;
                    }
                },
                mode -> mode.name().toLowerCase(Locale.ROOT));
    }

    public record Entry(Mode mode, String school, float ratio, float base, float coefficient,
                        Optional<String> outputType, Optional<String> sourceType,
                        Optional<Component> outputName,
                        Optional<Component> sourceName,
                        Optional<ResourceLocation> outputIcon) {

        private static final String DEFAULT_SOURCE_NAME = "damage dealt";
        private static final String DEFAULT_OUTPUT_NAME = "generic";

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Mode.CODEC.optionalFieldOf("mode", Mode.SPLIT).forGetter(Entry::mode),
                Codec.STRING.fieldOf("school").forGetter(Entry::school),
                Codec.FLOAT.optionalFieldOf("ratio", 0f).forGetter(Entry::ratio),
                Codec.FLOAT.optionalFieldOf("base", 0f).forGetter(Entry::base),
                Codec.FLOAT.fieldOf("spell_power_coefficient").forGetter(Entry::coefficient),
                Codec.STRING.optionalFieldOf("output_type").forGetter(Entry::outputType),
                Codec.STRING.optionalFieldOf("source_type").forGetter(Entry::sourceType),
                ComponentSerialization.CODEC
                        .optionalFieldOf("output_name").forGetter(Entry::outputName),
                ComponentSerialization.CODEC
                        .optionalFieldOf("source_name").forGetter(Entry::sourceName),
                ResourceLocation.CODEC
                        .optionalFieldOf("output_icon").forGetter(Entry::outputIcon)
        ).apply(instance, Entry::new));

        public Entry(Mode mode, String school, float ratio, float base, float coefficient) {
            this(mode, school, ratio, base, coefficient,
                    Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static Entry proportional(float share,
                                         Optional<String> outputType,
                                         Optional<String> sourceType,
                                         Optional<Component> outputName,
                                         Optional<Component> sourceName,
                                         Optional<ResourceLocation> outputIcon) {
            return new Entry(Mode.PROPORTIONAL, "", share, 0f, 0f,
                    outputType, sourceType, outputName, sourceName, outputIcon);
        }

        public boolean isSplit() {
            return mode == Mode.SPLIT;
        }

        public boolean matchesSourceType(String incomingTypeId) {
            return sourceType.isEmpty()
                    || (incomingTypeId != null && sourceType.get().equals(incomingTypeId));
        }

        public Component sourceDisplay() {
            return sourceName.orElseGet(() -> Component.literal(
                    sourceType.orElse(DEFAULT_SOURCE_NAME)));
        }

        public Component outputDisplay() {
            return outputName.orElseGet(() -> Component.literal(
                    outputType.orElse(DEFAULT_OUTPUT_NAME)));
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public float rawSplitTotal() {
        float total = 0f;
        for (Entry entry : entries) {
            if (entry.isSplit()) {
                total += Math.max(0f, entry.ratio());
            }
        }
        return total;
    }

    public float physicalFraction() {
        return Math.max(0f, 1f - Math.min(1f, rawSplitTotal()));
    }

    public float effectiveRatio(Entry entry) {
        if (!entry.isSplit()) {
            return 0f;
        }
        float raw = Math.max(0f, entry.ratio());
        float total = rawSplitTotal();
        return total > 1f ? raw / total : raw;
    }

    public DamageConversion with(Entry entry) {
        List<Entry> combined = new ArrayList<>(entries);
        combined.add(entry);
        return new DamageConversion(List.copyOf(combined));
    }

    public DamageConversion without(String school) {
        List<Entry> remaining = new ArrayList<>();
        for (Entry entry : entries) {
            if (!entry.school().equals(school)) {
                remaining.add(entry);
            }
        }
        return new DamageConversion(List.copyOf(remaining));
    }
}
