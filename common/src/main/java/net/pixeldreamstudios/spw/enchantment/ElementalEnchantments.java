package net.pixeldreamstudios.spw.enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.pixeldreamstudios.spw.component.DamageConversion;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.ArrayList;
import java.util.List;

public final class ElementalEnchantments {
    private ElementalEnchantments() {}

    public record Bonus(float base, float coefficient) {
        public static final Bonus NONE = new Bonus(0f, 0f);

        public boolean isEmpty() {
            return base <= 0f && coefficient <= 0f;
        }
    }

    public static Bonus perEntry(ItemStack weapon, DamageConversion conversion, RandomSource random) {
        if (weapon == null || weapon.isEmpty() || conversion == null) {
            return Bonus.NONE;
        }
        int scaled = scaledEntries(conversion);
        if (scaled == 0) {
            return Bonus.NONE;
        }
        float base = total(weapon, SpwEnchantmentEffects.ELEMENTAL_BASE, random);
        float coefficient = total(weapon, SpwEnchantmentEffects.ELEMENTAL_COEFFICIENT, random);
        return new Bonus(Math.max(0f, base) / scaled, Math.max(0f, coefficient) / scaled);
    }

    public static DamageConversion apply(DamageConversion conversion, Bonus bonus) {
        if (conversion == null || bonus.isEmpty()) {
            return conversion;
        }
        List<DamageConversion.Entry> adjusted = new ArrayList<>(conversion.entries().size());
        for (DamageConversion.Entry entry : conversion.entries()) {
            adjusted.add(isScaled(entry) ? entry.withBonus(bonus.base(), bonus.coefficient()) : entry);
        }
        return new DamageConversion(List.copyOf(adjusted));
    }

    public static DamageConversion apply(DamageConversion conversion, ItemStack weapon,
                                         RandomSource random) {
        return apply(conversion, perEntry(weapon, conversion, random));
    }

    private static boolean isScaled(DamageConversion.Entry entry) {
        return entry.mode() != DamageConversion.Mode.PROPORTIONAL;
    }

    private static int scaledEntries(DamageConversion conversion) {
        int count = 0;
        for (DamageConversion.Entry entry : conversion.entries()) {
            if (isScaled(entry)) {
                count++;
            }
        }
        return count;
    }

    private static float total(ItemStack weapon, DataComponentType<EnchantmentValueEffect> type,
                               RandomSource random) {
        if (type == null) {
            return 0f;
        }
        MutableFloat value = new MutableFloat(0f);
        for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : weapon.getEnchantments().entrySet()) {
            enchantment.getKey().value().modifyUnfilteredValue(type, random,
                    enchantment.getIntValue(), value);
        }
        return value.floatValue();
    }
}
