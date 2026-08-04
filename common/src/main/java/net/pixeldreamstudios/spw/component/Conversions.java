package net.pixeldreamstudios.spw.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public final class Conversions {
    private Conversions() {}

    public static DamageConversion of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        DamageConversion conversion = stack.get(SpwComponents.DAMAGE_CONVERSION);
        return conversion == null || conversion.isEmpty() ? null : conversion;
    }

    public static ItemStack orEmpty(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public static boolean suppressesPhysical(ItemStack stack) {
        return flag(stack, SpwComponents.SUPPRESS_PHYSICAL);
    }

    public static boolean hidesDamageLine(ItemStack stack) {
        return flag(stack, SpwComponents.HIDE_DAMAGE_LINE) || suppressesPhysical(stack);
    }

    private static boolean flag(ItemStack stack, DataComponentType<Boolean> type) {
        if (stack == null || stack.isEmpty() || type == null) {
            return false;
        }
        return Boolean.TRUE.equals(stack.get(type));
    }
}
