package net.pixeldreamstudios.spw.roll;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.component.SpwComponents;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.pixeldreamstudios.spw.damage.PhysicalReduction;

import java.util.List;

public final class RollApplier {
    private RollApplier() {}

    public static boolean tryRoll(ItemStack stack, RandomSource random) {
        if (stack.isEmpty() || hasRolled(stack)) {
            return false;
        }

        AuthoredConversion authored = RollTables.authoredFor(stack);
        RollConfig config = authored != null ? authored.asRoll() : RollTables.pick(stack, random);
        if (config == null) {
            return false;
        }

        DamageConversion rolled = rollFor(stack, config, random);
        if (rolled.isEmpty()) {
            return false;
        }

        if (!Conversions.set(stack, SpwComponents.DAMAGE_CONVERSION,
                SpwComponents.DAMAGE_CONVERSION_ID, rolled)) {
            return false;
        }
        PhysicalReduction.apply(stack, rolled.physicalFraction());

        if (config.mode().suppressesPhysical()) {
            Conversions.set(stack, SpwComponents.SUPPRESS_PHYSICAL,
                    SpwComponents.SUPPRESS_PHYSICAL_ID, Boolean.TRUE);
            Conversions.set(stack, SpwComponents.HIDE_DAMAGE_LINE,
                    SpwComponents.HIDE_DAMAGE_LINE_ID, Boolean.TRUE);
        }
        return true;
    }

    private static DamageConversion rollFor(ItemStack stack, RollConfig config, RandomSource random) {
        float chance = SpwConfig.inherentSchoolChance();
        List<String> inherent = chance > 0f ? InherentSchools.of(stack) : List.of();
        return ConversionRoller.roll(config, random, inherent, chance);
    }

    public static boolean hasRolled(ItemStack stack) {
        return Conversions.get(stack, SpwComponents.DAMAGE_CONVERSION,
                SpwComponents.DAMAGE_CONVERSION_ID) != null;
    }
}
