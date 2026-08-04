package net.pixeldreamstudios.spw.damage;

import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SchoolResolver {
    private SchoolResolver() {}

    private static final Set<String> WARNED = new HashSet<>();

    public static SpellSchool resolve(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            SpellSchool school = SpellSchools.getSchool(id);
            if (school == null && WARNED.add(id)) {
                SpellPoweredWeapons.LOGGER.warn(
                        "Unknown spell school '{}' on a weapon — entry skipped. "
                                + "Its defining mod may not be installed.", id);
            }
            return school;
        } catch (Exception e) {
            if (WARNED.add(id)) {
                SpellPoweredWeapons.LOGGER.warn("Malformed spell school id '{}' — entry skipped.", id);
            }
            return null;
        }
    }

    public static boolean isConvertible(SpellSchool school) {
        return school != null && school.archetype == SpellSchool.Archetype.MAGIC;
    }

    public static boolean isConvertible(String id) {
        return isConvertible(resolve(id));
    }

    public static List<String> convertibleIds() {
        return SpellSchools.all().stream()
                .filter(SchoolResolver::isConvertible)
                .map(school -> school.id.toString())
                .toList();
    }
}
