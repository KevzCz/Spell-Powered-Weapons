package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.damage.ArrowCharge;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.LaunchRecord;
import net.pixeldreamstudios.spw.damage.ProjectileHits;
import net.pixeldreamstudios.spw.damage.ProjectileOrigin;
import net.pixeldreamstudios.spw.damage.RangedDamage;
import net.pixeldreamstudios.spw.damage.SchoolRules;
import net.pixeldreamstudios.spw.damage.WeaponBasis;
import net.pixeldreamstudios.spw.damage.WeaponDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ProjectileHurtMixin {

    @Inject(method = "hurt", at = @At("RETURN"))
    private void spw$projectileElemental(DamageSource source, float amount,
                                         CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (target.level().isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }
        if (!cir.getReturnValueZ() && amount > 0f) {
            return;
        }
        Projectile shot = ProjectileHits.shotFor(source, target);
        if (shot == null || ProjectileHits.isBlacklisted(shot)
                || !(shot.getOwner() instanceof LivingEntity shooter)) {
            return;
        }
        ItemStack weapon = ProjectileOrigin.weaponFor(shot);
        if (Conversions.of(weapon) == null) {
            return;
        }
        if (!(shot instanceof LaunchRecord record) || !record.spw$claimHit(target)) {
            return;
        }
        float charge = shot instanceof AbstractArrow arrow ? ArrowCharge.of(arrow) : 1f;
        ElementalDamageDealer.deal(shooter, target, weapon, spw$basis(shot, shooter, weapon),
                SchoolRules.modesFor(source), charge);
    }

    @Unique
    private static float spw$basis(Projectile shot, LivingEntity shooter, ItemStack weapon) {
        if (shot instanceof ThrownTrident) {
            return WeaponDamage.originalOf(shooter, weapon);
        }
        if (shot instanceof AbstractArrow arrow) {
            return RangedDamage.originalOf(weapon, arrow.getBaseDamage());
        }
        return WeaponBasis.originalOf(shooter, weapon);
    }
}
