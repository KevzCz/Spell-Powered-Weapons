package net.pixeldreamstudios.spw.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.pixeldreamstudios.spw.damage.UseAction;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerGameMode.class)
public abstract class UseActionMixin {

    @WrapMethod(method = "useItem")
    private InteractionResult spw$markUse(ServerPlayer player, Level level, ItemStack stack,
                                          InteractionHand hand, Operation<InteractionResult> original) {
        Object previous = UseAction.begin(player, stack);
        try {
            return original.call(player, level, stack, hand);
        } finally {
            UseAction.end(previous);
        }
    }

    @WrapMethod(method = "useItemOn")
    private InteractionResult spw$markUseOn(ServerPlayer player, Level level, ItemStack stack,
                                            InteractionHand hand, BlockHitResult hit,
                                            Operation<InteractionResult> original) {
        Object previous = UseAction.begin(player, stack);
        try {
            return original.call(player, level, stack, hand, hit);
        } finally {
            UseAction.end(previous);
        }
    }
}
