package net.pixeldreamstudios.spw.config;

import java.util.ArrayList;
import java.util.List;

public final class CommonConfig {
    public boolean spread_damage_numbers_on_mobs = true;

    public List<String> projectile_blacklist = new ArrayList<>();

    public List<String> split_and_additive_schools = new ArrayList<>(List.of(
            "spell_power:physical_melee",
            "spell_power:physical_ranged"
    ));

    public List<String> additive_only_schools = new ArrayList<>(List.of(
            "spell_power:fire",
            "spell_power:frost",
            "spell_power:arcane",
            "spell_power:healing",
            "spell_power:lightning",
            "spell_power:soul",
            "spell_power:air",
            "spell_power:earth",
            "spell_power:water"
    ));
}
