package net.pixeldreamstudios.spw.compat;

import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.WeakHashMap;

public final class DamageNumberPositions {
    private DamageNumberPositions() {}

    private static final Map<Entity, Counter> COUNTERS = new WeakHashMap<>();

    private static final class Counter {
        private int next;
    }

    public static int next(Entity entity) {
        return COUNTERS.computeIfAbsent(entity, key -> new Counter()).next++;
    }
}
