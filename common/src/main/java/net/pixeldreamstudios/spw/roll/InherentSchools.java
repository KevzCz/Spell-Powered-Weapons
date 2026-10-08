package net.pixeldreamstudios.spw.roll;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.pixeldreamstudios.spw.damage.PhysicalReduction;
import net.pixeldreamstudios.spw.damage.SchoolResolver;
import net.spell_power.api.SpellSchool;

import java.util.ArrayList;
import java.util.List;

public final class InherentSchools {
    private InherentSchools() {}

    public static List<String> of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        ItemAttributeModifiers modifiers = PhysicalReduction.unreduced(stack);
        if (modifiers.modifiers().isEmpty()) {
            return List.of();
        }

        List<String> found = new ArrayList<>();
        for (String id : SchoolResolver.rollableIds()) {
            SpellSchool school = SchoolResolver.resolve(id);
            Holder<Attribute> attribute = school == null ? null : school.getAttributeEntry();
            if (attribute != null && grants(modifiers, attribute)) {
                found.add(id);
            }
        }
        return List.copyOf(found);
    }

    private static boolean grants(ItemAttributeModifiers modifiers, Holder<Attribute> attribute) {
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().value() == attribute.value() && entry.modifier().amount() > 0d) {
                return true;
            }
        }
        return false;
    }
}
