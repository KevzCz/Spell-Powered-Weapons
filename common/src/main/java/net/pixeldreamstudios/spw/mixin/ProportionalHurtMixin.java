package net.pixeldreamstudios.spw.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ProportionalHurtMixin {

    @Inject(method = "hurt", at = @At("RETURN"))
    private void spw$proportionalOnHurt(DamageSource source, float amount,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || amount <= 0f || ElementalDamageDealer.isDealing()) {
            return;
        }
        LivingEntity target = (LivingEntity) (Object) this;
        if (target.level().isClientSide()) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack weapon = spw$convertedHeld(attacker);
        if (Conversions.of(weapon) == null) {
            return;
        }

        ElementalDamageDealer.dealProportional(attacker, target, weapon,
                spw$typeId(source), amount);
    }

    @org.spongepowered.asm.mixin.Unique
    private static String spw$typeId(DamageSource source) {
        ResourceKey<DamageType> key = source.typeHolder().unwrapKey().orElse(null);
        return key == null ? null : key.location().toString();
    }

    @org.spongepowered.asm.mixin.Unique
    private static ItemStack spw$convertedHeld(LivingEntity attacker) {
        ItemStack main = attacker.getMainHandItem();
        if (Conversions.of(main) != null) {
            return main;
        }
        ItemStack off = attacker.getOffhandItem();
        return Conversions.of(off) != null ? off : ItemStack.EMPTY;
    }
}
