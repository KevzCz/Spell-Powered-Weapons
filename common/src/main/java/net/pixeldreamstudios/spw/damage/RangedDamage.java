package net.pixeldreamstudios.spw.damage;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;

public final class RangedDamage {
    private RangedDamage() {}

    public static float of(LivingEntity shooter, AbstractArrow arrow, double fallbackDamage) {
        double attribute = rangedDamageAttribute(shooter);
        double damage = Double.isNaN(attribute) ? fallbackDamage : attribute;
        return (float) Math.max(0d, damage);
    }

    public static float originalOf(ItemStack weapon, double fallbackDamage) {
        Holder<Attribute> attribute = rangedDamageAttribute();
        if (attribute == null || weapon == null || weapon.isEmpty()) {
            return (float) Math.max(0d, fallbackDamage);
        }

        float base = WeaponDamage.weaponAddValue(PhysicalReduction.unreduced(weapon),
                attr -> attr.value() == attribute.value());
        return base <= 0f ? (float) Math.max(0d, fallbackDamage) : base;
    }

    public static float chargeOf(AbstractArrow arrow) {
        return ArrowCharge.of(arrow);
    }

    public static float nominal(LivingEntity holder, ItemStack weapon) {
        Holder<Attribute> attribute = rangedDamageAttribute();
        if (attribute == null) {
            return Float.NaN;
        }
        double total = StackAttribute.of(holder, weapon, attribute, attribute::equals);
        return Double.isNaN(total) ? Float.NaN : (float) total;
    }

    private static Holder<Attribute> rangedDamageAttribute() {
        try {
            return EntityAttributes_RangedWeapon.DAMAGE.entry;
        } catch (Throwable t) {
            SpellPoweredWeapons.LOGGER.debug("Ranged damage attribute lookup failed", t);
            return null;
        }
    }

    private static double rangedDamageAttribute(LivingEntity holder) {
        if (holder == null) {
            return Double.NaN;
        }
        Holder<Attribute> attribute = rangedDamageAttribute();
        return attribute == null ? Double.NaN : holder.getAttributeValue(attribute);
    }

    public static boolean isRangedWeapon(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof ProjectileWeaponItem;
    }
}
