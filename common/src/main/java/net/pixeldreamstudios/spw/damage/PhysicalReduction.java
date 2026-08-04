package net.pixeldreamstudios.spw.damage;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;

import java.util.function.Consumer;

public final class PhysicalReduction {
    private PhysicalReduction() {}

    private static final String APPLIED_KEY = "spw_physical_fraction";
    private static final String DEBUG_PREFIX = "[spw-reduce] ";

    public static Consumer<String> debug = null;

    private static void dbg(String msg) {
        if (debug != null) {
            debug.accept(DEBUG_PREFIX + msg);
        }
    }

    public static void apply(ItemStack stack, float physicalFraction) {
        if (stack == null || stack.isEmpty()) {
            dbg("stack empty");
            return;
        }

        ItemAttributeModifiers defaults = unreduced(stack);

        dbg("baseline has " + defaults.modifiers().size() + " modifiers, fraction=" + physicalFraction);
        for (ItemAttributeModifiers.Entry e : defaults.modifiers()) {
            dbg("  " + e.attribute().getRegisteredName() + " amt=" + e.modifier().amount()
                    + " op=" + e.modifier().operation() + " slot=" + e.slot());
        }

        if (defaults.modifiers().isEmpty()) {
            dbg("baseline has no modifiers, nothing to reduce");
            return;
        }

        Holder<Attribute> ranged = rangedDamageAttribute();
        float fraction = Math.max(0f, Math.min(1f, physicalFraction));
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : defaults.modifiers()) {
            AttributeModifier modifier = entry.modifier();
            boolean reduce = fraction < 1f
                    && isDamage(entry.attribute(), ranged)
                    && modifier.operation() == AttributeModifier.Operation.ADD_VALUE;
            AttributeModifier scaled = reduce
                    ? new AttributeModifier(modifier.id(),
                            modifier.amount() * fraction, modifier.operation())
                    : modifier;
            if (reduce) {
                dbg("  reducing " + entry.attribute().getRegisteredName() + " "
                        + modifier.amount() + " -> " + scaled.amount());
            }
            builder.add(entry.attribute(), scaled, entry.slot());
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
                builder.build().withTooltip(defaults.showInTooltip()));
        recordApplied(stack, fraction);
        ItemAttributeModifiers check = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        dbg("wrote component; stack now has " + (check == null ? 0 : check.modifiers().size())
                + " modifiers");
    }

    public static float appliedFraction(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 1f;
        }
        CompoundTag tag = data.copyTag();
        return tag.contains(APPLIED_KEY) ? tag.getFloat(APPLIED_KEY) : 1f;
    }

    private static void recordApplied(ItemStack stack, float fraction) {
        if (fraction >= 1f) {
            CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
            if (existing != null && existing.copyTag().contains(APPLIED_KEY)) {
                stack.set(DataComponents.CUSTOM_DATA,
                        CustomData.of(withoutApplied(existing.copyTag())));
            }
            return;
        }
        CustomData current = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = current.copyTag();
        tag.putFloat(APPLIED_KEY, fraction);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static CompoundTag withoutApplied(CompoundTag tag) {
        tag.remove(APPLIED_KEY);
        return tag;
    }

    public static ItemAttributeModifiers unreduced(ItemStack stack) {
        ItemAttributeModifiers current = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (current == null || current.modifiers().isEmpty()) {
            return stack.getPrototype().getOrDefault(
                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        }

        float applied = appliedFraction(stack);
        if (applied >= 1f) {
            return current;
        }
        if (applied <= 0f) {
            return stack.getPrototype().getOrDefault(
                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        }

        Holder<Attribute> ranged = rangedDamageAttribute();
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            AttributeModifier modifier = entry.modifier();
            boolean restore = isDamage(entry.attribute(), ranged)
                    && modifier.operation() == AttributeModifier.Operation.ADD_VALUE;
            builder.add(entry.attribute(), restore
                    ? new AttributeModifier(modifier.id(),
                            modifier.amount() / applied, modifier.operation())
                    : modifier, entry.slot());
        }
        return builder.build().withTooltip(current.showInTooltip());
    }

    private static boolean isDamage(Holder<Attribute> attribute, Holder<Attribute> ranged) {
        if (attribute.is(Attributes.ATTACK_DAMAGE)) {
            return true;
        }
        return ranged != null && attribute.value() == ranged.value();
    }

    private static Holder<Attribute> rangedDamageAttribute() {
        try {
            return EntityAttributes_RangedWeapon.DAMAGE.entry;
        } catch (Throwable t) {
            SpellPoweredWeapons.LOGGER.debug("Ranged damage attribute lookup failed", t);
            return null;
        }
    }
}
