package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.FiredFromWeapon;
import net.pixeldreamstudios.spw.damage.RangedDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class ArrowAttackMixin {

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void spw$afterHit(EntityHitResult result, CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        if (self.level().isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }

        if (!(self.getOwner() instanceof LivingEntity shooter)) {
            return;
        }

        ItemStack weapon = Conversions.orEmpty(self.getWeaponItem());
        if (Conversions.of(weapon) == null && self instanceof FiredFromWeapon fired) {
            ItemStack launcher = fired.spw$getFiredFrom();
            if (Conversions.of(launcher) != null) {
                weapon = launcher;
            }
        }
        if (Conversions.of(weapon) == null) {
            return;
        }

        Entity target = result.getEntity();
        if (!(target instanceof LivingEntity)) {
            return;
        }

        float shotDamage = RangedDamage.originalOf(weapon, self.getBaseDamage());
        ElementalDamageDealer.deal(shooter, target, weapon, shotDamage,
                ElementalDamageDealer.WEAPON_MODES, RangedDamage.chargeOf(self));
    }
}
