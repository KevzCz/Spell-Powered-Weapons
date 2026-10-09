package net.pixeldreamstudios.spw.damage;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.spell_engine.entity.SpellProjectile;

public final class ProjectileHits {
    private ProjectileHits() {}

    private record Landing(Projectile projectile, Entity hit) {}

    private static final ThreadLocal<Landing> LANDING = new ThreadLocal<>();

    public static Object beginLanding(Projectile projectile, Entity hit) {
        Landing previous = LANDING.get();
        LANDING.set(new Landing(projectile, hit));
        return previous;
    }

    public static void endLanding(Object previous) {
        if (previous instanceof Landing landing) {
            LANDING.set(landing);
        } else {
            LANDING.remove();
        }
    }

    public static Projectile shotFor(DamageSource source, Entity target) {
        if (source == null || target == null) {
            return null;
        }
        if (source.getDirectEntity() instanceof Projectile direct) {
            return counts(direct) ? direct : null;
        }
        Landing landing = LANDING.get();
        if (landing == null || landing.hit() != target) {
            return null;
        }
        Entity owner = landing.projectile().getOwner();
        if (owner == null || source.getEntity() != owner) {
            return null;
        }
        Entity direct = source.getDirectEntity();
        if (direct != null && direct != owner) {
            return null;
        }
        return counts(landing.projectile()) ? landing.projectile() : null;
    }

    public static boolean isBlacklisted(Projectile projectile) {
        return SpwConfig.isBlacklistedProjectile(
                BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).toString());
    }

    private static boolean counts(Projectile projectile) {
        return !(projectile instanceof SpellProjectile);
    }
}
