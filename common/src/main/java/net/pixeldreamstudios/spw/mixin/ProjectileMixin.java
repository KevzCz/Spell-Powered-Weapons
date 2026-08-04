package net.pixeldreamstudios.spw.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.FiredFromWeapon;
import net.pixeldreamstudios.spw.damage.WeaponBasis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Projectile.class)
public abstract class ProjectileMixin implements FiredFromWeapon {

    @Unique
    private ItemStack spw$firedFrom = ItemStack.EMPTY;

    @Override
    public ItemStack spw$getFiredFrom() {
        return spw$firedFrom;
    }

    @Override
    public void spw$setFiredFrom(ItemStack weapon) {
        spw$firedFrom = weapon == null ? ItemStack.EMPTY : weapon;
    }

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void spw$captureWeapon(Entity owner, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (self.level().isClientSide() || !(owner instanceof LivingEntity living)) {
            return;
        }
        ItemStack launcher = spw$launchingItem(self, living);
        if (Conversions.of(launcher) != null) {
            spw$firedFrom = launcher.copy();
        }
    }

    @Unique
    private ItemStack spw$launchingItem(Projectile projectile, LivingEntity shooter) {
        if (projectile instanceof ItemSupplier supplier) {
            ItemStack thrown = supplier.getItem();
            if (spw$sameItem(thrown, shooter.getOffhandItem())) {
                return shooter.getOffhandItem();
            }
            return shooter.getMainHandItem();
        }
        return shooter.isUsingItem() ? shooter.getUseItem() : shooter.getMainHandItem();
    }

    @Unique
    private boolean spw$sameItem(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && a.getItem() == b.getItem();
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void spw$dealElemental(EntityHitResult result, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (self.level().isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }
        if (self instanceof AbstractArrow || spw$isBlacklisted(self)) {
            return;
        }
        if (!(self.getOwner() instanceof LivingEntity shooter)
                || !(result.getEntity() instanceof LivingEntity)) {
            return;
        }

        DamageConversion conversion = Conversions.of(spw$firedFrom);
        if (conversion == null) {
            return;
        }

        float basis = WeaponBasis.originalOf(shooter, spw$firedFrom);
        ElementalDamageDealer.deal(shooter, result.getEntity(), spw$firedFrom, basis,
                List.of(DamageConversion.Mode.SPLIT, DamageConversion.Mode.ADDITIVE));
    }

    @Unique
    private boolean spw$isBlacklisted(Projectile projectile) {
        return SpwConfig.isBlacklistedProjectile(
                BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).toString());
    }
}
