package net.pixeldreamstudios.spw.neoforge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.component.SpwComponents;
import net.pixeldreamstudios.spw.enchantment.SpwEnchantmentEffects;

@Mod(SpellPoweredWeapons.MOD_ID)
public final class SpwNeoForge {

    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SpellPoweredWeapons.MOD_ID);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<DamageConversion>>
            DAMAGE_CONVERSION = COMPONENTS.register(
                    SpwComponents.DAMAGE_CONVERSION_ID, SpwComponents::buildDamageConversion);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>>
            HIDE_DAMAGE_LINE = COMPONENTS.register(
                    SpwComponents.HIDE_DAMAGE_LINE_ID, SpwComponents::buildFlag);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>>
            SUPPRESS_PHYSICAL = COMPONENTS.register(
                    SpwComponents.SUPPRESS_PHYSICAL_ID, SpwComponents::buildFlag);

    private static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, SpellPoweredWeapons.MOD_ID);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentValueEffect>>
            ELEMENTAL_BASE = ENCHANTMENT_EFFECTS.register(
                    SpwEnchantmentEffects.ELEMENTAL_BASE_ID, SpwEnchantmentEffects::buildValueEffect);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentValueEffect>>
            ELEMENTAL_COEFFICIENT = ENCHANTMENT_EFFECTS.register(
                    SpwEnchantmentEffects.ELEMENTAL_COEFFICIENT_ID, SpwEnchantmentEffects::buildValueEffect);

    public SpwNeoForge(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ENCHANTMENT_EFFECTS.register(modBus);

        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(Registries.DATA_COMPONENT_TYPE)) {
                SpwComponents.DAMAGE_CONVERSION = DAMAGE_CONVERSION.get();
                SpwComponents.HIDE_DAMAGE_LINE = HIDE_DAMAGE_LINE.get();
                SpwComponents.SUPPRESS_PHYSICAL = SUPPRESS_PHYSICAL.get();
            }
            if (event.getRegistryKey().equals(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE)) {
                SpwEnchantmentEffects.ELEMENTAL_BASE = ELEMENTAL_BASE.get();
                SpwEnchantmentEffects.ELEMENTAL_COEFFICIENT = ELEMENTAL_COEFFICIENT.get();
            }
        });

        SpellPoweredWeapons.init();
    }
}
