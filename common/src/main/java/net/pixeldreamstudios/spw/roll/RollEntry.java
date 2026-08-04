package net.pixeldreamstudios.spw.roll;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record RollEntry(
        Optional<String> school,
        Ranged ratio,
        Ranged coefficient,
        Ranged base,
        Ranged share,
        Optional<String> sourceType,
        Optional<String> outputType,
        Optional<Component> sourceName,
        Optional<Component> outputName,
        Optional<ResourceLocation> outputIcon
) {
    public static final Codec<RollEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("school").forGetter(RollEntry::school),
            Ranged.CODEC.optionalFieldOf("ratio", Ranged.ZERO).forGetter(RollEntry::ratio),
            Ranged.CODEC.optionalFieldOf("coefficient", Ranged.ZERO).forGetter(RollEntry::coefficient),
            Ranged.CODEC.optionalFieldOf("base", Ranged.ZERO).forGetter(RollEntry::base),
            Ranged.CODEC.optionalFieldOf("share", Ranged.ZERO).forGetter(RollEntry::share),
            Codec.STRING.optionalFieldOf("source_type").forGetter(RollEntry::sourceType),
            Codec.STRING.optionalFieldOf("output_type").forGetter(RollEntry::outputType),
            ComponentSerialization.CODEC
                    .optionalFieldOf("source_name").forGetter(RollEntry::sourceName),
            ComponentSerialization.CODEC
                    .optionalFieldOf("output_name").forGetter(RollEntry::outputName),
            ResourceLocation.CODEC
                    .optionalFieldOf("output_icon").forGetter(RollEntry::outputIcon)
    ).apply(instance, RollEntry::new));

    public static RollEntry ofSchool(String school, Ranged ratio, Ranged coefficient,
                                     Ranged base, Ranged share) {
        return new RollEntry(Optional.of(school), ratio, coefficient, base, share,
                Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    public String schoolOrEmpty() {
        return school.orElse("");
    }
}
