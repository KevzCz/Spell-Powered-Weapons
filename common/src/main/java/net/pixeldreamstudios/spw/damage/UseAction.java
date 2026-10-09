package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class UseAction {
    private UseAction() {}

    private record Running(Entity user, ItemStack stack) {}

    private static final ThreadLocal<Running> RUNNING = new ThreadLocal<>();

    public static Object begin(Entity user, ItemStack stack) {
        Running previous = RUNNING.get();
        RUNNING.set(new Running(user, stack));
        return previous;
    }

    public static void end(Object previous) {
        if (previous instanceof Running running) {
            RUNNING.set(running);
        } else {
            RUNNING.remove();
        }
    }

    public static ItemStack launcherOf(LivingEntity shooter) {
        if (shooter == null) {
            return ItemStack.EMPTY;
        }
        if (shooter.isUsingItem()) {
            return shooter.getUseItem();
        }
        Running running = RUNNING.get();
        return running != null && running.user() == shooter ? running.stack() : ItemStack.EMPTY;
    }
}
