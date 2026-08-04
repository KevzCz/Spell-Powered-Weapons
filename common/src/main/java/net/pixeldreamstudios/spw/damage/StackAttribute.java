package net.pixeldreamstudios.spw.damage;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public final class StackAttribute {
    private StackAttribute() {}

    public static double of(LivingEntity holder, ItemStack stack,
                            Holder<Attribute> attribute,
                            Predicate<Holder<Attribute>> matches) {
        if (holder == null || attribute == null) {
            return Double.NaN;
        }
        AttributeInstance instance = holder.getAttribute(attribute);
        if (instance == null) {
            return Double.NaN;
        }

        double base = instance.getBaseValue();
        if (stack == null || stack.isEmpty()) {
            return Math.max(0d, base);
        }

        double[] additive = {0d};
        double[] multiplyBase = {0d};
        double[] multiplyTotal = {1d};

        stack.forEachModifier(EquipmentSlot.MAINHAND, (candidate, modifier) -> {
            if (!matches.test(candidate)) {
                return;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> additive[0] += modifier.amount();
                case ADD_MULTIPLIED_BASE -> multiplyBase[0] += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal[0] *= 1d + modifier.amount();
            }
        });

        double total = (base + additive[0]) * (1d + multiplyBase[0]) * multiplyTotal[0];
        return Math.max(0d, total);
    }
}
