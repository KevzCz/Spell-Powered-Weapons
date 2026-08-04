package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class WeaponBasis {
    private WeaponBasis() {}

    public static float originalOf(LivingEntity shooter, ItemStack weapon) {
        if (RangedDamage.isRangedWeapon(weapon)) {
            return RangedDamage.originalOf(weapon, 0d);
        }
        return WeaponDamage.originalOf(shooter, weapon);
    }
}
