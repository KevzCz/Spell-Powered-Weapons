package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class HitThrottle {
    private HitThrottle() {}

    private static final int IFRAME_THRESHOLD = 10;

    public static boolean isThrottled(Entity target) {
        return target instanceof LivingEntity living
                && living.invulnerableTime > IFRAME_THRESHOLD;
    }
}
