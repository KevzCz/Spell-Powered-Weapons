package net.pixeldreamstudios.spw.roll;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AuthoredConversion(
        RollMode mode,
        List<ItemVerifier> verifiers,
        List<ItemVerifier> excludes,
        List<RollEntry> entries
) {
    public static final Codec<AuthoredConversion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RollMode.CODEC.optionalFieldOf("mode", RollMode.SPLIT).forGetter(AuthoredConversion::mode),
            ItemVerifier.CODEC.listOf().optionalFieldOf("verifiers", List.of()).forGetter(AuthoredConversion::verifiers),
            ItemVerifier.CODEC.listOf().optionalFieldOf("excludes", List.of()).forGetter(AuthoredConversion::excludes),
            RollEntry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(AuthoredConversion::entries)
    ).apply(instance, AuthoredConversion::new));

    public boolean matches(ItemStack stack) {
        return ItemVerifier.matchesAny(stack, verifiers)
                && !ItemVerifier.matchesAny(stack, excludes);
    }

    private static final String AUTHORED_PATH = "authored";
    private static final int UNUSED_WEIGHT = 1;

    public RollConfig asRoll() {
        return new RollConfig(
                ResourceLocation.fromNamespaceAndPath(RollTables.ROLL_ID_NAMESPACE, AUTHORED_PATH),
                mode, UNUSED_WEIGHT, verifiers, excludes, entries);
    }
}
