package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.WeaponDamage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin {

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void spw$dealElemental(EntityHitResult result, CallbackInfo ci) {
        ThrownTrident self = (ThrownTrident) (Object) this;
        if (self.level().isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }
        if (!(self.getOwner() instanceof LivingEntity shooter)
                || !(result.getEntity() instanceof LivingEntity)) {
            return;
        }

        ItemStack weapon = self.getWeaponItem();
        DamageConversion conversion = Conversions.of(weapon);
        if (conversion == null) {
            return;
        }

        float basis = WeaponDamage.originalOf(shooter, weapon);
        ElementalDamageDealer.deal(shooter, result.getEntity(), weapon, basis,
                List.of(DamageConversion.Mode.SPLIT, DamageConversion.Mode.ADDITIVE));
    }
}
