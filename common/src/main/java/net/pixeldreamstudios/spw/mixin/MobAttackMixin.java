package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.HitThrottle;
import net.pixeldreamstudios.spw.damage.WeaponDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Mob.class)
public abstract class MobAttackMixin {

    @Unique
    private boolean spw$targetWasInvulnerable;

    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void spw$captureThrottle(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob) (Object) this;
        if (!self.level().isClientSide() && !ElementalDamageDealer.isDealing()) {
            spw$targetWasInvulnerable = HitThrottle.isThrottled(target);
        }
    }

    @Inject(method = "doHurtTarget", at = @At("RETURN"))
    private void spw$afterAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob) (Object) this;
        if (self.level().isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }

        boolean throttled = spw$targetWasInvulnerable;
        spw$targetWasInvulnerable = false;
        if (throttled || !cir.getReturnValueZ()) {
            return;
        }

        ItemStack weapon = self.getMainHandItem();
        if (Conversions.of(weapon) == null) {
            return;
        }

        ElementalDamageDealer.deal(self, target, weapon, WeaponDamage.originalOf(self, weapon),
                List.of(DamageConversion.Mode.SPLIT, DamageConversion.Mode.ADDITIVE));
    }
}
