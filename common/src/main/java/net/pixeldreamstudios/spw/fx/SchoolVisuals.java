package net.pixeldreamstudios.spw.fx;

import net.pixeldreamstudios.spw.config.SpwConfig;
import net.spell_engine.api.spell.fx.ParticleGroup;
import net.spell_power.api.SpellSchool;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SchoolVisuals {
    private SchoolVisuals() {}

    private static final Map<String, String> DEFAULTS = new LinkedHashMap<>();

    static {
        DEFAULTS.put("spell_power:fire", "spell_engine:flame_spark");
        DEFAULTS.put("spell_power:frost", "spell_engine:frost_shard");
        DEFAULTS.put("spell_power:lightning", "spell_engine:electric_arc_a");
        DEFAULTS.put("spell_power:soul", "spell_engine:dripping_blood");
        DEFAULTS.put("spell_power:earth", "more_rpg_classes:stone_particle");
        DEFAULTS.put("spell_power:water", "more_rpg_classes:water_drop");
        DEFAULTS.put("spell_power:air", "more_rpg_classes:small_gust");
        DEFAULTS.put("spell_power:nature", "more_rpg_classes:leaf");
    }

    private static final String FALLBACK = "minecraft:enchanted_hit";

    private static final float MIN_SPEED = 0.1f;
    private static final float MAX_SPEED = 0.35f;
    private static final float MIN_COUNT = 2f;
    private static final float MAX_COUNT = 8f;
    private static final float COUNT_FACTOR = 8f;
    private static final float BASE_SCALE = 0.8f;
    private static final float SCALE_FACTOR = 0.5f;

    public static Map<String, String> defaults(Iterable<String> schoolIds) {
        Map<String, String> seed = new LinkedHashMap<>(DEFAULTS);
        for (String id : schoolIds) {
            seed.putIfAbsent(id, FALLBACK);
        }
        return seed;
    }

    public static ParticleGroup impact(SpellSchool school, float intensity) {
        ParticleGroup group = new ParticleGroup();
        group.id = particleId(school);
        group.batch(batch -> batch
                .shape(ParticleGroup.Shape.SPHERE)
                .anchor(ParticleGroup.Anchor.ENTITY)
                .count(countFor(intensity))
                .speed(MIN_SPEED, MAX_SPEED));
        group.appearance(appearance -> appearance
                .scale(BASE_SCALE + intensity * SCALE_FACTOR));
        return group;
    }

    private static float countFor(float intensity) {
        return Math.max(MIN_COUNT, Math.min(MAX_COUNT, MIN_COUNT + intensity * COUNT_FACTOR));
    }

    private static String particleId(SpellSchool school) {
        if (school == null) {
            return FALLBACK;
        }
        String configured = SpwConfig.particleFor(school.id.toString());
        return configured.isEmpty() ? FALLBACK : configured;
    }
}
