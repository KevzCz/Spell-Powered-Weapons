package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.roll.RollApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Inject(method = "remove", at = @At("RETURN"))
    private void spw$rollOnPickup(int amount, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack removed = cir.getReturnValue();
        if (removed == null || removed.isEmpty()) {
            return;
        }
        Player player = ((ResultSlotAccessor) this).spw$getPlayer();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        RollApplier.tryRoll(removed, player.getRandom());
    }
}
