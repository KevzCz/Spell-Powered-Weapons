package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.pixeldreamstudios.spw.roll.RollApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class QuickCraftMixin {

    @Inject(
            method = "doClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;"
                            + "quickMoveStack(Lnet/minecraft/world/entity/player/Player;I)"
                            + "Lnet/minecraft/world/item/ItemStack;"))
    private void spw$rollBeforeQuickMove(int slotId, int button,
                                         ClickType clickType,
                                         Player player, CallbackInfo ci) {
        if (player.level().isClientSide()) {
            return;
        }
        List<Slot> slots = ((AbstractContainerMenu) (Object) this).slots;
        if (slotId < 0 || slotId >= slots.size()) {
            return;
        }
        Slot slot = slots.get(slotId);
        if (slot instanceof ResultSlot && slot.hasItem()) {
            RollApplier.tryRoll(slot.getItem(), player.getRandom());
        }
    }
}
