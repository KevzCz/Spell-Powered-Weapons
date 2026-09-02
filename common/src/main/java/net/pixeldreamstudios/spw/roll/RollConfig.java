package net.pixeldreamstudios.spw.roll;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record RollConfig(
        ResourceLocation id,
        RollMode mode,
        int weight,
        List<ItemVerifier> verifiers,
        List<ItemVerifier> excludes,
        List<RollEntry> entries
) {
    public static final int DEFAULT_WEIGHT = 10;

    public static final Codec<Parsed> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RollMode.CODEC.optionalFieldOf("mode", RollMode.PLAIN).forGetter(Parsed::mode),
            Codec.INT.optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(Parsed::weight),
            ItemVerifier.CODEC.listOf().optionalFieldOf("verifiers", List.of()).forGetter(Parsed::verifiers),
            ItemVerifier.CODEC.listOf().optionalFieldOf("excludes", List.of()).forGetter(Parsed::excludes),
            RollEntry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(Parsed::entries)
    ).apply(instance, Parsed::new));

    public record Parsed(RollMode mode, int weight, List<ItemVerifier> verifiers,
                         List<ItemVerifier> excludes, List<RollEntry> entries) {
        public RollConfig withId(ResourceLocation id) {
            return new RollConfig(id, mode, weight, verifiers, excludes, entries);
        }
    }

    public boolean matches(ItemStack stack) {
        return ItemVerifier.matchesAny(stack, verifiers)
                && !ItemVerifier.matchesAny(stack, excludes);
    }
}
