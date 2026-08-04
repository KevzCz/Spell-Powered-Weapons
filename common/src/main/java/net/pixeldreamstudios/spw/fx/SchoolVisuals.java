package net.pixeldreamstudios.spw.fx;

import net.pixeldreamstudios.spw.config.SpwConfig;
import net.spell_engine.api.spell.fx.ParticleBatch;
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

    public static Map<String, String> defaults(Iterable<String> schoolIds) {
        Map<String, String> seed = new LinkedHashMap<>(DEFAULTS);
        for (String id : schoolIds) {
            seed.putIfAbsent(id, FALLBACK);
        }
        return seed;
    }

    public static ParticleBatch impact(SpellSchool school, float intensity) {
        ParticleBatch batch = new ParticleBatch(
                particleId(school),
                ParticleBatch.Shape.SPHERE,
                ParticleBatch.Origin.CENTER,
                countFor(intensity),
                0.1f,
                0.35f);
        batch.scale = 0.8f + intensity * 0.5f;
        return batch;
    }

    private static float countFor(float intensity) {
        return Math.max(2f, Math.min(8f, 2f + intensity * 8f));
    }

    private static String particleId(SpellSchool school) {
        if (school == null) {
            return FALLBACK;
        }
        String configured = SpwConfig.particleFor(school.id.toString());
        return configured.isEmpty() ? FALLBACK : configured;
    }
}
