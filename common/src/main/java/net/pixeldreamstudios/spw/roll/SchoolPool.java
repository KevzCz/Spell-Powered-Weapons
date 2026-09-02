package net.pixeldreamstudios.spw.roll;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.pixeldreamstudios.spw.damage.SchoolResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record SchoolPool(List<WeightedSchool> choices, boolean any) {

    public static final SchoolPool ANY = new SchoolPool(List.of(), true);

    private static final String ANY_KEYWORD = "any";
    private static final int DEFAULT_WEIGHT = 1;

    public record WeightedSchool(String school, int weight) {
        public static final Codec<WeightedSchool> OBJECT_CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.STRING.fieldOf("school").forGetter(WeightedSchool::school),
                        Codec.INT.optionalFieldOf("weight", DEFAULT_WEIGHT).forGetter(WeightedSchool::weight)
                ).apply(instance, WeightedSchool::new));

        public static final Codec<WeightedSchool> CODEC = Codec.either(
                Codec.STRING.xmap(id -> new WeightedSchool(id, DEFAULT_WEIGHT), WeightedSchool::school),
                OBJECT_CODEC).xmap(either -> either.map(value -> value, value -> value), Either::left);
    }

    private static final Codec<SchoolPool> LIST_CODEC = WeightedSchool.CODEC.listOf()
            .xmap(list -> new SchoolPool(list, false), SchoolPool::choices);

    private static final Codec<SchoolPool> SINGLE_CODEC = Codec.STRING.xmap(
            id -> ANY_KEYWORD.equalsIgnoreCase(id)
                    ? ANY
                    : new SchoolPool(List.of(new WeightedSchool(id, DEFAULT_WEIGHT)), false),
            pool -> pool.any() || pool.choices().isEmpty()
                    ? ANY_KEYWORD
                    : pool.choices().get(0).school());

    public static final Codec<SchoolPool> CODEC = Codec.either(SINGLE_CODEC, LIST_CODEC)
            .xmap(either -> either.map(value -> value, value -> value), Either::left);

    public static SchoolPool of(String school) {
        return ANY_KEYWORD.equalsIgnoreCase(school)
                ? ANY
                : new SchoolPool(List.of(new WeightedSchool(school, DEFAULT_WEIGHT)), false);
    }

    public boolean isSingle() {
        return !any && choices.size() == 1;
    }

    public Optional<String> single() {
        return isSingle() ? Optional.of(choices.get(0).school()) : Optional.empty();
    }

    public List<String> declared() {
        List<String> out = new ArrayList<>(choices.size());
        for (WeightedSchool choice : choices) {
            out.add(choice.school());
        }
        return out;
    }

    public String pick(RandomSource random, Predicate<String> valid, Collection<String> taken) {
        return pick(random, valid, taken, SchoolResolver::rollableIds);
    }

    public String pick(RandomSource random, Predicate<String> valid, Collection<String> taken,
                       Supplier<List<String>> anySource) {
        List<WeightedSchool> pool = candidates(valid, taken, anySource);
        if (pool.isEmpty()) {
            pool = candidates(valid, List.of(), anySource);
        }
        if (pool.isEmpty()) {
            return null;
        }

        int total = 0;
        for (WeightedSchool choice : pool) {
            total += Math.max(1, choice.weight());
        }
        int target = random.nextInt(Math.max(1, total));
        for (WeightedSchool choice : pool) {
            target -= Math.max(1, choice.weight());
            if (target < 0) {
                return choice.school();
            }
        }
        return pool.get(pool.size() - 1).school();
    }

    private List<WeightedSchool> candidates(Predicate<String> valid, Collection<String> taken,
                                            Supplier<List<String>> anySource) {
        List<WeightedSchool> pool = new ArrayList<>();
        for (WeightedSchool choice : source(anySource)) {
            if (!taken.contains(choice.school()) && valid.test(choice.school())) {
                pool.add(choice);
            }
        }
        return pool;
    }

    private List<WeightedSchool> source(Supplier<List<String>> anySource) {
        if (!any) {
            return choices;
        }
        List<WeightedSchool> all = new ArrayList<>();
        for (String id : anySource.get()) {
            all.add(new WeightedSchool(id, DEFAULT_WEIGHT));
        }
        return all;
    }
}
