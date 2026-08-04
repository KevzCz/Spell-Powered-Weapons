package net.pixeldreamstudios.spw.damage;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.Predicate;

public final class WeaponDamage {
    private WeaponDamage() {}

    private static final double BASE_ATTACK_DAMAGE = 1.0d;

    public static float of(LivingEntity attacker, ItemStack weapon) {
        double total = StackAttribute.of(attacker, weapon, Attributes.ATTACK_DAMAGE,
                attribute -> attribute.is(Attributes.ATTACK_DAMAGE));

        return Double.isNaN(total)
                ? (float) BASE_ATTACK_DAMAGE
                : (float) Math.max(0d, total);
    }

    public static float originalOf(LivingEntity attacker, ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            return 0f;
        }
        return weaponAddValue(PhysicalReduction.unreduced(weapon),
                attribute -> attribute.is(Attributes.ATTACK_DAMAGE));
    }

    public static float attackBonusOf(LivingEntity attacker, ItemStack weapon, Entity target) {
        if (weapon == null || weapon.isEmpty() || target == null
                || !(attacker instanceof Player player)) {
            return 0f;
        }
        try {
            DamageSource source = player.damageSources().playerAttack(player);
            return Math.max(0f, weapon.getItem().getAttackDamageBonus(target, 0f, source));
        } catch (Throwable ignored) {
            return 0f;
        }
    }

    static float weaponAddValue(ItemAttributeModifiers modifiers,
                                Predicate<Holder<Attribute>> matches) {
        double additive = 0d;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (matches.test(entry.attribute())
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                additive += entry.modifier().amount();
            }
        }
        return (float) Math.max(0d, additive);
    }
}
