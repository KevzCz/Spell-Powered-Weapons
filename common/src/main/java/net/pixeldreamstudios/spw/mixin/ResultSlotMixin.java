package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.pixeldreamstudios.spw.roll.RollApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public abstract class ResultSlotMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void spw$rollOnCraft(Player taker, ItemStack stack, CallbackInfo ci) {
        if (taker.level().isClientSide()
                || !SpwConfig.rollOnCraft()) {
            return;
        }
        RollApplier.tryRoll(stack, taker.getRandom());
    }
}
