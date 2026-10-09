package net.pixeldreamstudios.spw.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.damage.LaunchRecord;
import net.pixeldreamstudios.spw.damage.ProjectileHits;
import net.pixeldreamstudios.spw.damage.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(Projectile.class)
public abstract class ProjectileMixin implements LaunchRecord {

    @Unique
    private static final String SPW$LAUNCHER = "spw_launcher";

    @Unique
    private static final String SPW$HANDS = "spw_hands";

    @Unique
    private ItemStack spw$launcher = ItemStack.EMPTY;

    @Unique
    private final List<ItemStack> spw$hands = new ArrayList<>();

    @Unique
    private final Set<UUID> spw$claimed = new HashSet<>();

    @Override
    public ItemStack spw$launcher() {
        return spw$launcher;
    }

    @Override
    public List<ItemStack> spw$hands() {
        return spw$hands;
    }

    @Override
    public boolean spw$claimHit(Entity target) {
        return target != null && spw$claimed.add(target.getUUID());
    }

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void spw$recordLaunch(Entity owner, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (self.level().isClientSide() || !(owner instanceof LivingEntity living)) {
            return;
        }
        ItemStack launcher = UseAction.launcherOf(living);
        spw$launcher = Conversions.of(launcher) != null ? launcher.copy() : ItemStack.EMPTY;
        spw$hands.clear();
        spw$keepConverted(living.getMainHandItem());
        spw$keepConverted(living.getOffhandItem());
    }

    @Unique
    private void spw$keepConverted(ItemStack hand) {
        if (Conversions.of(hand) != null) {
            spw$hands.add(hand.copy());
        }
    }

    @WrapMethod(method = "hitTargetOrDeflectSelf")
    private ProjectileDeflection spw$scopeLanding(HitResult result, Operation<ProjectileDeflection> original) {
        Projectile self = (Projectile) (Object) this;
        Entity hit = result instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        if (self.level().isClientSide() || hit == null) {
            return original.call(result);
        }
        Object previous = ProjectileHits.beginLanding(self, hit);
        try {
            return original.call(result);
        } finally {
            ProjectileHits.endLanding(previous);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void spw$saveLaunch(CompoundTag tag, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        if (!spw$launcher.isEmpty()) {
            tag.put(SPW$LAUNCHER, spw$launcher.save(self.registryAccess()));
        }
        if (!spw$hands.isEmpty()) {
            ListTag hands = new ListTag();
            for (ItemStack hand : spw$hands) {
                hands.add(hand.save(self.registryAccess()));
            }
            tag.put(SPW$HANDS, hands);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void spw$readLaunch(CompoundTag tag, CallbackInfo ci) {
        Projectile self = (Projectile) (Object) this;
        spw$launcher = tag.contains(SPW$LAUNCHER, Tag.TAG_COMPOUND)
                ? ItemStack.parse(self.registryAccess(), tag.getCompound(SPW$LAUNCHER)).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        spw$hands.clear();
        ListTag hands = tag.getList(SPW$HANDS, Tag.TAG_COMPOUND);
        for (int index = 0; index < hands.size(); index++) {
            ItemStack.parse(self.registryAccess(), hands.getCompound(index)).ifPresent(spw$hands::add);
        }
    }
}
