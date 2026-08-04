package net.pixeldreamstudios.spw.roll;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.damage.PhysicalReduction;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.component.SpwComponents;

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

        DamageConversion rolled = ConversionRoller.roll(config, random);
        if (rolled.isEmpty()) {
            return false;
        }

        stack.set(SpwComponents.DAMAGE_CONVERSION, rolled);
        PhysicalReduction.apply(stack, rolled.physicalFraction());

        if (config.mode().suppressesPhysical()) {
            stack.set(SpwComponents.SUPPRESS_PHYSICAL, Boolean.TRUE);
            stack.set(SpwComponents.HIDE_DAMAGE_LINE, Boolean.TRUE);
        }
        return true;
    }

    public static boolean hasRolled(ItemStack stack) {
        return stack.has(SpwComponents.DAMAGE_CONVERSION);
    }
}
