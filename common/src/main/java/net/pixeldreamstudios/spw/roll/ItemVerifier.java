package net.pixeldreamstudios.spw.roll;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record ItemVerifier(Optional<String> id, Optional<String> tag) {

    private static final Codec<ItemVerifier> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("id").forGetter(ItemVerifier::id),
            Codec.STRING.optionalFieldOf("tag").forGetter(ItemVerifier::tag)
    ).apply(instance, ItemVerifier::new));

    private static final Codec<ItemVerifier> STRING_CODEC = Codec.STRING.xmap(
            raw -> raw.startsWith("#")
                    ? new ItemVerifier(Optional.empty(), Optional.of(raw.substring(1)))
                    : new ItemVerifier(Optional.of(raw), Optional.empty()),
            verifier -> verifier.tag()
                    .map(tag -> "#" + tag)
                    .orElseGet(() -> verifier.id().orElse("")));

    public static final Codec<ItemVerifier> CODEC = Codec.either(STRING_CODEC, OBJECT_CODEC)
            .xmap(either -> either.map(value -> value, value -> value), Either::left);

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (id.isPresent()) {
            ResourceLocation itemId = ResourceLocation.tryParse(id.get());
            return itemId != null && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId);
        }
        if (tag.isPresent()) {
            ResourceLocation tagId = ResourceLocation.tryParse(tag.get());
            return tagId != null && stack.is(TagKey.create(Registries.ITEM, tagId));
        }
        return false;
    }

    public static boolean matchesAny(ItemStack stack, List<ItemVerifier> verifiers) {
        for (ItemVerifier verifier : verifiers) {
            if (verifier.matches(stack)) {
                return true;
            }
        }
        return false;
    }
}
