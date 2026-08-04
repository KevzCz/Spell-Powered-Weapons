package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.Map;
import java.util.WeakHashMap;

public final class ArrowCharge {
    private ArrowCharge() {}

    private static final Map<AbstractArrow, Float> CHARGES = new WeakHashMap<>();

    private static final Map<LivingEntity, Float> PENDING = new WeakHashMap<>();

    public static void expect(LivingEntity shooter, float charge) {
        if (shooter != null) {
            PENDING.put(shooter, charge);
        }
    }

    public static void clearExpectation(LivingEntity shooter) {
        if (shooter != null) {
            PENDING.remove(shooter);
        }
    }

    public static void attach(LivingEntity shooter, AbstractArrow arrow) {
        if (shooter == null || arrow == null) {
            return;
        }
        Float charge = PENDING.get(shooter);
        if (charge != null) {
            CHARGES.put(arrow, charge);
        }
    }

    public static float of(AbstractArrow arrow) {
        if (arrow == null) {
            return 1f;
        }
        Float charge = CHARGES.get(arrow);
        return charge == null ? 1f : Math.max(0f, Math.min(1f, charge));
    }
}
