package net.pixeldreamstudios.spw.enchantment;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;

public final class SpwEnchantmentEffects {
    private SpwEnchantmentEffects() {}

    public static final String ELEMENTAL_BASE_ID = "elemental_base";

    public static final String ELEMENTAL_COEFFICIENT_ID = "elemental_coefficient";

    public static DataComponentType<EnchantmentValueEffect> ELEMENTAL_BASE;

    public static DataComponentType<EnchantmentValueEffect> ELEMENTAL_COEFFICIENT;

    public static DataComponentType<EnchantmentValueEffect> buildValueEffect() {
        return DataComponentType.<EnchantmentValueEffect>builder()
                .persistent(EnchantmentValueEffect.CODEC)
                .build();
    }

    public static void register() {
        ELEMENTAL_BASE = Registry.register(
                BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
                SpellPoweredWeapons.id(ELEMENTAL_BASE_ID),
                buildValueEffect());
        ELEMENTAL_COEFFICIENT = Registry.register(
                BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
                SpellPoweredWeapons.id(ELEMENTAL_COEFFICIENT_ID),
                buildValueEffect());
    }
}
