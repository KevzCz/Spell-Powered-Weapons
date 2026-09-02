package net.pixeldreamstudios.spw.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Conversions {
    private Conversions() {}

    private static final Set<String> MISSING = ConcurrentHashMap.newKeySet();

    public static DamageConversion of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        DamageConversion conversion = get(stack, SpwComponents.DAMAGE_CONVERSION,
                SpwComponents.DAMAGE_CONVERSION_ID);
        return conversion == null || conversion.isEmpty() ? null : conversion;
    }

    public static ItemStack orEmpty(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public static boolean suppressesPhysical(ItemStack stack) {
        return flag(stack, SpwComponents.SUPPRESS_PHYSICAL, SpwComponents.SUPPRESS_PHYSICAL_ID);
    }

    public static boolean hidesDamageLine(ItemStack stack) {
        return flag(stack, SpwComponents.HIDE_DAMAGE_LINE, SpwComponents.HIDE_DAMAGE_LINE_ID)
                || suppressesPhysical(stack);
    }

    public static <T> boolean set(ItemStack stack, DataComponentType<T> type, String id, T value) {
        if (stack == null || stack.isEmpty() || !available(type, id)) {
            return false;
        }
        stack.set(type, value);
        return true;
    }

    public static <T> T get(ItemStack stack, DataComponentType<T> type, String id) {
        if (stack == null || stack.isEmpty() || !available(type, id)) {
            return null;
        }
        return stack.get(type);
    }

    public static boolean remove(ItemStack stack, DataComponentType<?> type, String id) {
        if (stack == null || stack.isEmpty() || !available(type, id)) {
            return false;
        }
        stack.remove(type);
        return true;
    }

    private static boolean flag(ItemStack stack, DataComponentType<Boolean> type, String id) {
        return Boolean.TRUE.equals(get(stack, type, id));
    }

    private static boolean available(DataComponentType<?> type, String id) {
        if (type != null) {
            return true;
        }
        if (MISSING.add(id)) {
            SpellPoweredWeapons.LOGGER.error(
                    "Data component '{}' was never registered on this loader. Reads and writes for "
                            + "it are skipped so items stay saveable — this is a mod bug, please report it.",
                    id);
        }
        return false;
    }
}
