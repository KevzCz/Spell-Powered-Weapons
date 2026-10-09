package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.List;

public final class SchoolRules {
    private SchoolRules() {}

    private static final List<DamageConversion.Mode> ADDITIVE_ONLY =
            List.of(DamageConversion.Mode.ADDITIVE);

    public static List<DamageConversion.Mode> modesFor(SpellSchool school) {
        return allowsSplit(school) ? ElementalDamageDealer.WEAPON_MODES : ADDITIVE_ONLY;
    }

    public static List<DamageConversion.Mode> modesFor(DamageSource source) {
        return modesFor(schoolOf(source));
    }

    public static SpellSchool schoolOf(DamageSource source) {
        if (source == null) {
            return null;
        }
        for (SpellSchool school : SpellSchools.all()) {
            if (school.damageType != null && source.is(school.damageType)) {
                return school;
            }
        }
        return null;
    }

    public static boolean allowsSplit(SpellSchool school) {
        if (school == null) {
            return true;
        }
        boolean archetypeDefault = school.archetype != SpellSchool.Archetype.MAGIC;
        return SpwConfig.schoolAllowsSplit(school.id.toString(), archetypeDefault);
    }
}
