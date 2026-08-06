package net.pixeldreamstudios.spw.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.pixeldreamstudios.spw.roll.RollApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootTable.class)
public abstract class LootTableMixin {

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"))
    private void spw$rollGeneratedLoot(LootParams params,
                                       CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> items = cir.getReturnValue();
        if (items == null || items.isEmpty()) {
            return;
        }
        for (ItemStack stack : items) {
            RollApplier.tryRoll(stack, params.getLevel().getRandom());
        }
    }

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;J)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"))
    private void spw$rollSeededLoot(LootParams params, long seed,
                                    CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> items = cir.getReturnValue();
        if (items == null || items.isEmpty()) {
            return;
        }
        for (ItemStack stack : items) {
            RollApplier.tryRoll(stack, params.getLevel().getRandom());
        }
    }
}
