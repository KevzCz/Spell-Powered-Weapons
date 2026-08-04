package net.pixeldreamstudios.spw.mixin;

import net.fabric_extras.ranged_weapon.internal.ArrowExtension;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.damage.ArrowCharge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class ArrowChargeMixin {

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void spw$recordCharge(ItemStack stack, Level level, LivingEntity shooter,
                                  int timeLeft, CallbackInfo ci) {
        int useTicks = stack.getUseDuration(shooter) - timeLeft;
        ArrowCharge.expect(shooter, BowItem.getPowerForTime(useTicks));
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void spw$clearCharge(ItemStack stack, Level level, LivingEntity shooter,
                                 int timeLeft, CallbackInfo ci) {
        ArrowCharge.clearExpectation(shooter);
    }

    @Inject(method = "shootProjectile", at = @At("RETURN"))
    private void spw$attachCharge(LivingEntity shooter, Projectile projectile, int index,
                                  float velocity, float inaccuracy, float angle,
                                  LivingEntity target, CallbackInfo ci) {
        if (projectile instanceof AbstractArrow arrow) {
            ArrowCharge.attach(shooter, arrow);
            spw$suppressPhysical(shooter, arrow);
        }
    }

    @Unique
    private void spw$suppressPhysical(LivingEntity shooter, AbstractArrow arrow) {
        if (shooter == null || shooter.level().isClientSide()) {
            return;
        }
        ItemStack bow = Conversions.orEmpty(arrow.getWeaponItem());
        if (bow.isEmpty()) {
            bow = shooter.getUseItem();
        }
        if (!Conversions.suppressesPhysical(bow)) {
            return;
        }

        arrow.setBaseDamage(0d);
        arrow.setCritArrow(false);
        spw$markRangedWeaponApiHandled(arrow);
    }

    @Unique
    private void spw$markRangedWeaponApiHandled(AbstractArrow arrow) {
        try {
            if (arrow instanceof ArrowExtension extension) {
                extension.rwa_markModified(true);
            }
        } catch (Throwable absent) {
            SpellPoweredWeapons.LOGGER.debug("Ranged Weapon API arrow flag unavailable", absent);
        }
    }
}
