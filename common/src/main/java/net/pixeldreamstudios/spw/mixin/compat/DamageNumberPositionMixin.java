package net.pixeldreamstudios.spw.mixin.compat;

import net.mehvahdjukaar.dummmmmmy.common.TargetDummyEntity;
import net.mehvahdjukaar.dummmmmmy.network.ClientBoundDamageNumberMessage;
import net.minecraft.world.entity.Entity;
import net.pixeldreamstudios.spw.compat.DamageNumberPositions;
import net.pixeldreamstudios.spw.config.SpwConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(ClientBoundDamageNumberMessage.class)
public abstract class DamageNumberPositionMixin {

    @ModifyArg(
            method = "handle",
            at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/dummmmmmy/network/"
                    + "ClientBoundDamageNumberMessage;spawnNumber(Lnet/minecraft/world/entity/"
                    + "Entity;I)V"),
            index = 1,
            require = 0)
    private int spw$positionFor(Entity entity, int position) {
        if (!SpwConfig.spreadDamageNumbersOnMobs()
                || entity == null
                || entity instanceof TargetDummyEntity) {
            return position;
        }
        return DamageNumberPositions.next(entity);
    }
}
