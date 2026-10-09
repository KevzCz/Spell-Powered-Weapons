package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;

public final class ProjectileOrigin {
    private ProjectileOrigin() {}

    public static ItemStack weaponFor(Projectile projectile) {
        if (projectile == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stated = projectile.getWeaponItem();
        if (stated != null) {
            return converted(stated);
        }
        ItemStack own = projectile instanceof ItemSupplier supplier ? supplier.getItem() : ItemStack.EMPTY;
        if (Conversions.of(own) != null) {
            return own;
        }
        if (!(projectile instanceof LaunchRecord record)) {
            return ItemStack.EMPTY;
        }
        if (!own.isEmpty()) {
            for (ItemStack hand : record.spw$hands()) {
                if (hand.is(own.getItem())) {
                    return hand;
                }
            }
        }
        return converted(record.spw$launcher());
    }

    private static ItemStack converted(ItemStack stack) {
        return Conversions.of(stack) != null ? stack : ItemStack.EMPTY;
    }
}
