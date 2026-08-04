package net.pixeldreamstudios.spw.roll;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public record RollOverride(
        List<ItemVerifier> verifiers,
        boolean replace,
        List<String> include,
        List<String> exclude
) {
    private static final String OVERRIDE_TYPE = "override";
    private static final String MODE_PREFIX = "mode:";
    private static final String GLOB_SUFFIX = "*";

    public static final Codec<RollOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("type", OVERRIDE_TYPE).forGetter(o -> OVERRIDE_TYPE),
            ItemVerifier.CODEC.listOf().optionalFieldOf("verifiers", List.of()).forGetter(RollOverride::verifiers),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(RollOverride::replace),
            Codec.STRING.listOf().optionalFieldOf("include", List.of()).forGetter(RollOverride::include),
            Codec.STRING.listOf().optionalFieldOf("exclude", List.of()).forGetter(RollOverride::exclude)
    ).apply(instance, (type, verifiers, replace, include, exclude) ->
            new RollOverride(verifiers, replace, include, exclude)));

    public boolean matches(ItemStack stack) {
        return ItemVerifier.matchesAny(stack, verifiers);
    }

    public boolean reincludes(RollConfig roll) {
        return !replace && matchesAny(include, roll);
    }

    static boolean matchesAny(List<String> patterns, RollConfig roll) {
        String id = roll.id().toString();
        String mode = roll.mode().name().toLowerCase(Locale.ROOT);
        for (String pattern : patterns) {
            if (pattern.startsWith(MODE_PREFIX)) {
                if (mode.equals(pattern.substring(MODE_PREFIX.length()).toLowerCase(Locale.ROOT))) {
                    return true;
                }
            } else if (pattern.endsWith(GLOB_SUFFIX)) {
                if (id.startsWith(pattern.substring(0, pattern.length() - 1))) {
                    return true;
                }
            } else if (id.equals(pattern)) {
                return true;
            }
        }
        return false;
    }
}
