package net.pixeldreamstudios.spw.roll;

import net.minecraft.util.RandomSource;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.damage.SchoolResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ConversionRoller {
    private ConversionRoller() {}

    public static DamageConversion roll(RollConfig config, RandomSource random) {
        return roll(config, random, school -> SchoolResolver.resolve(school) != null);
    }

    public static DamageConversion roll(RollConfig config, RandomSource random,
                                        Predicate<String> schoolIsValid) {
        return roll(config, random, schoolIsValid, SchoolResolver::rollableIds);
    }

    public static DamageConversion roll(RollConfig config, RandomSource random,
                                        Predicate<String> schoolIsValid,
                                        Supplier<List<String>> anySource) {
        if (config == null || !config.mode().producesConversion() || config.entries().isEmpty()) {
            return DamageConversion.EMPTY;
        }

        RollMode mode = config.mode();
        List<DamageConversion.Entry> built = new ArrayList<>(config.entries().size());
        Set<String> used = new HashSet<>();

        for (RollEntry entry : config.entries()) {
            String school = resolveSchool(entry, random, schoolIsValid, used, anySource);
            if (entry.schools().isPresent() && school == null) {
                continue;
            }
            if (school != null) {
                used.add(school);
            }
            built.add(buildEntry(mode, entry, school, random));
        }

        return built.isEmpty() ? DamageConversion.EMPTY : new DamageConversion(List.copyOf(built));
    }

    private static String resolveSchool(RollEntry entry, RandomSource random,
                                        Predicate<String> schoolIsValid, Set<String> used,
                                        Supplier<List<String>> anySource) {
        if (entry.schools().isEmpty()) {
            return null;
        }
        return entry.schools().get().pick(random, schoolIsValid, used, anySource);
    }

    private static DamageConversion.Entry buildEntry(RollMode mode, RollEntry entry, String school,
                                                     RandomSource random) {
        float ratio;
        float coefficient = Math.max(0f, entry.coefficient().sample(random));
        float base = Math.max(0f, entry.base().sample(random));

        if (mode == RollMode.PROPORTIONAL) {
            return DamageConversion.Entry.proportional(
                    clamp01(entry.share().sample(random)),
                    entry.outputType(), entry.sourceType(),
                    entry.outputName(), entry.sourceName(), entry.outputIcon());
        }

        switch (mode) {
            case SPLIT -> ratio = clamp01(entry.ratio().sample(random));
            case FULL_ELEMENTAL -> ratio = 1f;
            case ADDITIVE -> ratio = 0f;
            default -> ratio = 0f;
        }

        return new DamageConversion.Entry(mode.componentMode(), school == null ? "" : school,
                ratio, base, coefficient);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
