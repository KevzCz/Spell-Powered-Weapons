package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.HitThrottle;
import net.pixeldreamstudios.spw.damage.WeaponDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {

    @Unique
    private float spw$swingCharge = 1f;

    @Unique
    private boolean spw$targetWasInvulnerable;

    @Unique
    private float spw$attackBonus;

    @ModifyArg(
            method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 1)
    private float spw$suppressPhysical(float amount) {
        return spw$suppressed() ? 0f : amount;
    }

    @ModifyArg(
            method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 1)
    private float spw$suppressSweepPhysical(float amount) {
        return spw$suppressed() ? 0f : amount;
    }

    @Unique
    private boolean spw$suppressed() {
        Player self = (Player) (Object) this;
        if (ElementalDamageDealer.isDealing()) {
            return false;
        }
        return Conversions.suppressesPhysical(self.getMainHandItem());
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void spw$captureSwing(Entity target, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }
        spw$swingCharge = self.getAttackStrengthScale(0.5f);
        spw$targetWasInvulnerable = HitThrottle.isThrottled(target);
        spw$attackBonus = WeaponDamage.attackBonusOf(self, self.getMainHandItem(), target);
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void spw$afterAttack(Entity target, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self.level().isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }

        boolean throttled = spw$targetWasInvulnerable;
        float charge = spw$swingCharge;
        float bonus = spw$attackBonus;
        spw$targetWasInvulnerable = false;
        spw$swingCharge = 1f;
        spw$attackBonus = 0f;
        if (throttled) {
            return;
        }

        ItemStack weapon = self.getMainHandItem();
        if (Conversions.of(weapon) == null) {
            return;
        }

        float basis = WeaponDamage.originalOf(self, weapon) + bonus;
        ElementalDamageDealer.deal(self, target, weapon, basis,
                ElementalDamageDealer.WEAPON_MODES, charge);
    }
}
