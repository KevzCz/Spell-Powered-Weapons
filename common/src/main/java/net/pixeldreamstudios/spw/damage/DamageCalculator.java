package net.pixeldreamstudios.spw.damage;

import net.pixeldreamstudios.spw.component.DamageConversion;

import java.util.ArrayList;
import java.util.List;

public final class DamageCalculator {
    private DamageCalculator() {}

    public record Portion(String school, float amount, DamageConversion.Mode mode) {}

    public record Breakdown(float physical, List<Portion> elemental) {
        public float elementalTotal() {
            float total = 0f;
            for (Portion portion : elemental) {
                total += portion.amount();
            }
            return total;
        }

        public float total() {
            return physical + elementalTotal();
        }
    }

    public static float elementalAmount(float weaponDamage, float effectiveRatio,
                                        float base, float coefficient, float spellPower) {
        return elementalAmount(weaponDamage, effectiveRatio, base, coefficient, spellPower, 1f);
    }

    public static float elementalAmount(float weaponDamage, float effectiveRatio, float base,
                                        float coefficient, float spellPower, float charge) {
        float floor = weaponDamage * Math.max(0f, effectiveRatio);
        float scaled = spellPower * coefficient;
        float total = floor + Math.max(0f, base) + scaled;
        return Math.max(0f, total * Math.max(0f, charge));
    }

    public static Breakdown breakdown(DamageConversion conversion, float weaponDamage,
                                      PowerLookup powerLookup) {
        if (conversion == null || conversion.isEmpty()) {
            return new Breakdown(weaponDamage, List.of());
        }

        float physical = weaponDamage * conversion.physicalFraction();
        List<Portion> portions = new ArrayList<>();

        for (DamageConversion.Entry entry : conversion.entries()) {
            float power = powerLookup.powerOf(entry.school());
            if (Float.isNaN(power)) {
                continue;
            }
            float amount = elementalAmount(weaponDamage, conversion.effectiveRatio(entry),
                    entry.base(), entry.coefficient(), power);
            if (amount > 0f) {
                portions.add(new Portion(entry.school(), amount, entry.mode()));
            }
        }
        return new Breakdown(physical, List.copyOf(portions));
    }

    @FunctionalInterface
    public interface PowerLookup {
        float powerOf(String schoolId);
    }
}
