package net.pixeldreamstudios.spw.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.pixeldreamstudios.spw.roll.RollApplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LootTableMixin {

    @ModifyVariable(
            method = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private Consumer<ItemStack> spw$rollGeneratedLoot(Consumer<ItemStack> output, LootContext context) {
        if (context == null || !((LootContextAccessor) context).spw$visitedElements().isEmpty()) {
            return output;
        }
        return stack -> {
            RollApplier.tryRoll(stack, context.getRandom());
            output.accept(stack);
        };
    }
}
