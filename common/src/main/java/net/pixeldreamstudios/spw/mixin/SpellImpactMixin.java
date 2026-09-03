package net.pixeldreamstudios.spw.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.WeaponBasis;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellExecution;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_power.api.SpellSchool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpellImpacts.class)
public abstract class SpellImpactMixin {

    @Inject(
            method = "performImpacts(Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/world/entity/LivingEntity;"
                    + "Lnet/minecraft/world/entity/Entity;"
                    + "Lnet/minecraft/world/entity/Entity;"
                    + "Lnet/minecraft/core/Holder;Ljava/util/List;"
                    + "Lnet/spell_engine/internals/SpellExecution$ImpactContext;)Z",
            at = @At("TAIL"), remap = false)
    private static void spw$onPerformImpacts(Level level, LivingEntity caster, Entity target,
                                             Entity source, Holder<Spell> spell,
                                             List<Spell.Impact> impacts,
                                             SpellExecution.ImpactContext context,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || impacts == null || !spw$hasDamage(impacts)) {
            return;
        }
        if (level.isClientSide() || ElementalDamageDealer.isDealing()) {
            return;
        }
        if (caster == null || !(target instanceof LivingEntity)) {
            return;
        }

        ItemStack weapon = spw$convertedHeld(caster);
        DamageConversion conversion = Conversions.of(weapon);
        if (conversion == null) {
            return;
        }

        List<DamageConversion.Mode> modes = spw$allowsSplit(spw$spellSchool(spell))
                ? List.of(DamageConversion.Mode.SPLIT, DamageConversion.Mode.ADDITIVE)
                : List.of(DamageConversion.Mode.ADDITIVE);

        float basis = WeaponBasis.originalOf(caster, weapon);
        float scale = context != null ? Math.max(0f, context.total(spell)) : 1f;
        ElementalDamageDealer.deal(caster, target, weapon, basis, modes, scale);
    }

    @Unique
    private static boolean spw$hasDamage(List<Spell.Impact> impacts) {
        for (Spell.Impact impact : impacts) {
            if (impact != null && impact.action != null
                    && impact.action.type == Spell.Impact.Action.Type.DAMAGE) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static SpellSchool spw$spellSchool(Holder<Spell> spell) {
        if (spell == null || !spell.isBound()) {
            return null;
        }
        return spell.value().school;
    }

    @Unique
    private static boolean spw$allowsSplit(SpellSchool school) {
        try {
            if (school == null) {
                return true;
            }
            boolean archetypeDefault = school.archetype != SpellSchool.Archetype.MAGIC;
            return SpwConfig.schoolAllowsSplit(school.id.toString(), archetypeDefault);
        } catch (Throwable t) {
            return true;
        }
    }

    @Unique
    private static ItemStack spw$convertedHeld(LivingEntity caster) {
        ItemStack main = caster.getMainHandItem();
        if (Conversions.of(main) != null) {
            return main;
        }
        ItemStack off = caster.getOffhandItem();
        return Conversions.of(off) != null ? off : ItemStack.EMPTY;
    }
}
