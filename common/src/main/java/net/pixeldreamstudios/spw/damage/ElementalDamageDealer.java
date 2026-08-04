package net.pixeldreamstudios.spw.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.compat.ModCompat;
import net.pixeldreamstudios.spw.compat.MoreRpgCompat;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.fx.SchoolVisuals;
import net.pixeldreamstudios.spw.mixin.LivingEntityAccessor;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.compat.CriticalStrikeCompat;
import net.spell_engine.fx.ParticleHelper;
import net.spell_power.api.SpellDamageSource;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;

import java.util.List;
import java.util.Random;

public final class ElementalDamageDealer {
    private ElementalDamageDealer() {}

    private static final ThreadLocal<Boolean> DEALING = ThreadLocal.withInitial(() -> false);

    public static final List<DamageConversion.Mode> WEAPON_MODES =
            List.of(DamageConversion.Mode.SPLIT, DamageConversion.Mode.ADDITIVE);

    private static final Random RNG = new Random();

    public static boolean isDealing() {
        return DEALING.get();
    }

    public static float deal(LivingEntity attacker, Entity target, ItemStack weapon,
                             float weaponDamage, List<DamageConversion.Mode> modes) {
        return deal(attacker, target, weapon, weaponDamage, modes, 1f);
    }

    public static float deal(LivingEntity attacker, Entity target, ItemStack weapon,
                             float weaponDamage, List<DamageConversion.Mode> modes, float charge) {
        if (DEALING.get() || !(target instanceof LivingEntity living) || living.isRemoved()) {
            return 0f;
        }

        DamageConversion conversion = Conversions.of(weapon);
        if (conversion == null) {
            return 0f;
        }

        float dealt = 0f;
        DEALING.set(true);
        try {
            for (DamageConversion.Entry entry : conversion.entries()) {
                if (!modes.contains(entry.mode())) {
                    continue;
                }

                SpellSchool school = SchoolResolver.resolve(entry.school());
                if (!SchoolResolver.isConvertible(school)) {
                    continue;
                }

                float amount = DamageCalculator.elementalAmount(
                        weaponDamage,
                        conversion.effectiveRatio(entry),
                        entry.base(),
                        entry.coefficient(),
                        spellPowerOf(school, attacker),
                        charge);

                amount *= weaknessMultiplier(school, living);

                DamageSource source = attacker instanceof Player player
                        ? SpellDamageSource.player(school, player)
                        : SpellDamageSource.mob(school, attacker);

                if (rollCritical(school, attacker)) {
                    float multiplier = criticalMultiplier(school, attacker);
                    amount *= multiplier;
                    CriticalStrikeCompat.setCriticalStrike(source, multiplier);
                }

                if (amount <= 0f) {
                    continue;
                }

                if (hurtBypassingIFrames(living, source, amount)) {
                    dealt += amount;
                    float share = entry.isSplit()
                            ? conversion.effectiveRatio(entry)
                            : Math.min(1f, entry.coefficient());
                    ParticleHelper.sendBatches(living, new ParticleBatch[]{
                            SchoolVisuals.impact(school, share)});
                }
            }
        } finally {
            DEALING.set(false);
        }
        return dealt;
    }

    public static float dealProportional(LivingEntity attacker, Entity target, ItemStack weapon,
                                         String incomingTypeId, float incomingAmount) {
        if (DEALING.get() || incomingAmount <= 0f
                || !(target instanceof LivingEntity living) || living.isRemoved()) {
            return 0f;
        }
        DamageConversion conversion = Conversions.of(weapon);
        if (conversion == null) {
            return 0f;
        }

        float dealt = 0f;
        DEALING.set(true);
        try {
            for (DamageConversion.Entry entry : conversion.entries()) {
                if (entry.mode() != DamageConversion.Mode.PROPORTIONAL) {
                    continue;
                }
                if (!entry.matchesSourceType(incomingTypeId)) {
                    continue;
                }

                float amount = incomingAmount * Math.max(0f, entry.ratio());
                if (amount <= 0f) {
                    continue;
                }

                DamageSource source = DamageTypeOutput.source(attacker, entry.outputType().orElse(null));
                if (source == null) {
                    continue;
                }
                if (hurtBypassingIFrames(living, source, amount)) {
                    dealt += amount;
                }
            }
        } finally {
            DEALING.set(false);
        }
        return dealt;
    }

    private static boolean hurtBypassingIFrames(LivingEntity target, DamageSource source, float amount) {
        LivingEntityAccessor accessor = (LivingEntityAccessor) target;
        int previousInvulnerable = target.invulnerableTime;
        float previousLastHurt = accessor.spw$getLastHurt();

        target.invulnerableTime = 0;
        accessor.spw$setLastHurt(0f);
        try {
            return target.hurt(source, amount);
        } finally {
            target.invulnerableTime = Math.max(previousInvulnerable, target.invulnerableTime);
            accessor.spw$setLastHurt(Math.max(previousLastHurt, accessor.spw$getLastHurt()));
        }
    }

    private static float weaknessMultiplier(SpellSchool school, LivingEntity target) {
        if (!ModCompat.hasMoreRpgLibrary()) {
            return 1f;
        }
        return MoreRpgCompat.weaknessMultiplier(school, target);
    }

    public static boolean rollCritical(SpellSchool school, LivingEntity entity) {
        double chance = SpellPower.getSpellPower(school, entity).criticalChance();
        return chance > 0d && RNG.nextDouble() < chance;
    }

    public static float criticalMultiplier(SpellSchool school, LivingEntity entity) {
        return (float) Math.max(1d, SpellPower.getSpellPower(school, entity).criticalDamage());
    }

    public static float spellPowerOf(SpellSchool school, LivingEntity entity) {
        return (float) SpellPower.getSpellPower(school, entity).nonCriticalValue();
    }

    public static float spellPowerOf(SpellSchool school, LivingEntity entity, ItemStack weapon) {
        if (school == null) {
            return 0f;
        }
        var attribute = school.getAttributeEntry();
        if (attribute == null) {
            return spellPowerOf(school, entity);
        }

        double total = StackAttribute.of(entity, weapon, attribute, attribute::equals);
        if (Double.isNaN(total)) {
            return spellPowerOf(school, entity);
        }

        float live = spellPowerOf(school, entity);
        var instance = entity == null ? null : entity.getAttribute(attribute);
        double equipped = instance == null ? 0d : instance.getValue();
        return (float) Math.max(0d, live + (total - equipped));
    }
}
