package net.pixeldreamstudios.spw.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.weakness.ScopedWeakness;
import net.spell_power.api.SpellSchool;
import net.more_rpg_classes.custom.MoreSpellSchoolWeakness;

import java.util.List;

public final class MoreRpgCompat {
    private MoreRpgCompat() {}

    public static float weaknessMultiplier(SpellSchool school, LivingEntity target) {
        if (school == null || target == null) {
            return 1f;
        }
        try {
            List<ScopedWeakness> weaknesses = MoreSpellSchoolWeakness.getWeaknesses(school);
            if (weaknesses == null || weaknesses.isEmpty()) {
                return 1f;
            }

            float multiplier = 1f;
            for (ScopedWeakness weakness : weaknesses) {
                if (weakness.impact_type() != Spell.Impact.Action.Type.DAMAGE) {
                    continue;
                }
                Spell.Impact.TargetModifier entry = weakness.weakness();
                if (entry == null || entry.modifier == null || !appliesTo(entry, target)) {
                    continue;
                }
                multiplier *= Math.max(0f, entry.modifier.power_multiplier);
            }
            return multiplier;
        } catch (Throwable t) {
            SpellPoweredWeapons.LOGGER.debug("More RPG Library weakness lookup failed", t);
            return 1f;
        }
    }

    private static boolean appliesTo(Spell.Impact.TargetModifier entry, LivingEntity target) {
        List<Spell.TargetCondition> conditions = entry.conditions;
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        for (Spell.TargetCondition condition : conditions) {
            boolean matched = matches(condition, target);
            if (entry.all_required && !matched) {
                return false;
            }
            if (!entry.all_required && matched) {
                return true;
            }
        }
        return entry.all_required;
    }

    private static boolean matches(Spell.TargetCondition condition, LivingEntity target) {
        if (condition == null) {
            return true;
        }

        float healthPercent = target.getMaxHealth() <= 0f
                ? 1f
                : target.getHealth() / target.getMaxHealth();
        if (condition.health_percent_above > 0f && healthPercent <= condition.health_percent_above) {
            return false;
        }
        if (condition.health_percent_below > 0f && healthPercent >= condition.health_percent_below) {
            return false;
        }

        String entityType = condition.entity_type;
        if (entityType == null || entityType.isBlank()) {
            return true;
        }
        String tag = entityType.startsWith("#") ? entityType.substring(1) : entityType;
        ResourceLocation id = ResourceLocation.tryParse(tag);
        if (id == null) {
            return false;
        }
        return target.getType().is(TagKey.create(Registries.ENTITY_TYPE, id));
    }
}
