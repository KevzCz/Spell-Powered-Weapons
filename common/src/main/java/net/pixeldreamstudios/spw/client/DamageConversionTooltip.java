package net.pixeldreamstudios.spw.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.component.SpwComponents;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.pixeldreamstudios.spw.damage.DamageCalculator;
import net.pixeldreamstudios.spw.damage.ElementalDamageDealer;
import net.pixeldreamstudios.spw.damage.RangedDamage;
import net.pixeldreamstudios.spw.damage.SchoolResolver;
import net.pixeldreamstudios.spw.damage.WeaponDamage;
import net.spell_power.api.SpellSchool;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DamageConversionTooltip {
    private DamageConversionTooltip() {}

    private static final String VANILLA_ATTRIBUTE_LINE_KEY = "attribute.modifier.equals.0";

    private static final String ATTACK_DAMAGE_ATTRIBUTE_KEY = "attribute.name.generic.attack_damage";

    private static final String RANGED_DAMAGE_ATTRIBUTE_KEY = "attribute.name.ranged_weapon.damage";

    private static final String SCHOOL_DAMAGE_KEY = "tooltip.spell_powered_weapons.school_damage";
    private static final String UNRESOLVED_KEY = "tooltip.spell_powered_weapons.unresolved";
    private static final String ITEM_DAMAGE_KEY = "tooltip.spell_powered_weapons.item_damage";
    private static final String SPLIT_MODE_KEY = "tooltip.spell_powered_weapons.mode.split";
    private static final String ADDITIVE_MODE_KEY = "tooltip.spell_powered_weapons.mode.additive";

    private static final String ATTRIBUTE_NAME_PREFIX = "attribute.name.";
    private static final String MODIFIER_EQUALS_PREFIX = "attribute.modifier.equals.";
    private static final String MODIFIER_PLUS_PREFIX = "attribute.modifier.plus.";

    private static final String[] SCHOOL_NAME_SUFFIXES = {" Spell Power", " Power"};

    private static final int MAX_COMPONENT_DEPTH = 8;

    public static void append(ItemStack stack, List<Component> lines) {
        if (stack.isEmpty()) {
            return;
        }
        DamageConversion conversion = stack.get(SpwComponents.DAMAGE_CONVERSION);
        if (conversion == null || conversion.isEmpty()) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        boolean ranged = RangedDamage.isRangedWeapon(stack);
        float rawDamage = ranged
                ? RangedDamage.originalOf(stack, Float.NaN)
                : WeaponDamage.originalOf(player, stack);

        if (Float.isNaN(rawDamage)) {
            return;
        }
        float weaponDamage = Math.max(0f, rawDamage);

        DamageCalculator.Breakdown breakdown = DamageCalculator.breakdown(
                conversion, weaponDamage, schoolId -> {
                    SpellSchool school = SchoolResolver.resolve(schoolId);
                    if (!SchoolResolver.isConvertible(school)) {
                        return Float.NaN;
                    }
                    return ElementalDamageDealer.spellPowerOf(school, player, stack);
                });

        boolean advanced = Minecraft.getInstance().options.advancedItemTooltips
                && SpwConfig.showModeTags();
        boolean detailed = Screen.hasShiftDown() && SpwConfig.showDamageBreakdown();
        List<Component> elemental = new ArrayList<>();
        for (DamageCalculator.Portion portion : breakdown.elemental()) {
            SpellSchool school = SchoolResolver.resolve(portion.school());
            elemental.add(schoolLine(portion, school, advanced));
            if (detailed) {
                elemental.add(breakdownLine(portion, conversion, weaponDamage, player, stack));
            }
        }
        for (DamageConversion.Entry entry : conversion.entries()) {
            if (entry.mode() == DamageConversion.Mode.PROPORTIONAL) {
                elemental.add(proportionalLine(entry));
            } else if (!SchoolResolver.isConvertible(entry.school())) {
                elemental.add(withLeading(
                        IconLeading.split(""),
                        attributeLine("?",
                                Component.translatable(UNRESOLVED_KEY, entry.school()),
                                ChatFormatting.RED.getColor())));
            }
        }

        if (elemental.isEmpty()) {
            return;
        }

        int damageLine = indexOfDamageLine(lines, ranged);
        if (damageLine < 0) {
            lines.addAll(elemental);
            return;
        }

        if (Conversions.hidesDamageLine(stack)) {
            lines.remove(damageLine);
            lines.addAll(damageLine, elemental);
            return;
        }

        lines.addAll(damageLine + 1, elemental);
        if (detailed) {
            lines.add(damageLine + 1, physicalBreakdownLine(conversion, weaponDamage));
        }
    }

    private static Component withLeading(IconLeading.Split split, Component body) {
        String prefix = split.hasIcon() ? split.icon() + " " : " ";
        return Component.empty().append(Component.literal(prefix)).append(body);
    }

    private static final String PROPORTIONAL_KEY = "tooltip.spell_powered_weapons.proportional";

    private static Component proportionalLine(DamageConversion.Entry entry) {
        IconLeading.Split outputSplit = IconLeading.split(entry.outputDisplay().getString());
        Component output = Component.literal(outputSplit.text().trim());

        MutableComponent body = Component.translatable(PROPORTIONAL_KEY,
                Component.literal(percentText(entry.ratio())),
                entry.sourceDisplay(),
                output);
        return withLeading(outputSplit, body.withStyle(ChatFormatting.AQUA));
    }

    private static String percentText(float value) {
        return Math.round(value * 100f) + "%";
    }

    private static Component schoolLine(DamageCalculator.Portion portion, SpellSchool school,
                                        boolean advanced) {
        IconLeading.Split split = IconLeading.split(rawSchoolName(portion.school()));
        Component label = Component.translatable(
                SCHOOL_DAMAGE_KEY, Component.literal(shortSchoolName(split.text())));
        MutableComponent body = attributeLine(
                format(portion.amount()), label, school == null ? null : school.color);

        if (advanced && portion.mode() != null) {
            body.append(Component.literal(" "))
                    .append(Component.translatable(modeTagKey(portion.mode()))
                            .withStyle(ChatFormatting.DARK_GRAY));
        }

        return withLeading(split, body);
    }

    private static Component physicalBreakdownLine(DamageConversion conversion, float weaponDamage) {
        String text = format(weaponDamage) + " "
                + Component.translatable(ITEM_DAMAGE_KEY).getString()
                + " (×" + formatRatio(conversion.physicalFraction()) + ")";
        return subLine(text);
    }

    private static Component breakdownLine(DamageCalculator.Portion portion,
                                           DamageConversion conversion, float weaponDamage,
                                           Player player, ItemStack stack) {
        DamageConversion.Entry entry = entryFor(conversion, portion);
        if (entry == null) {
            return subLine(format(portion.amount()));
        }

        SpellSchool school = SchoolResolver.resolve(portion.school());
        StringBuilder text = new StringBuilder();

        float ratio = conversion.effectiveRatio(entry);
        if (ratio > 0f) {
            text.append('(').append(format(weaponDamage))
                    .append(" × ").append(formatRatio(ratio)).append(')');
        }
        if (entry.base() > 0f) {
            appendTerm(text, format(entry.base()));
        }
        float scaled = entry.coefficient() <= 0f || school == null
                ? 0f
                : ElementalDamageDealer.spellPowerOf(school, player, stack) * entry.coefficient();
        if (scaled > 0f) {
            appendTerm(text, format(scaled));
        }
        if (text.length() == 0) {
            text.append(format(portion.amount()));
        }
        return subLine(text.toString());
    }

    private static void appendTerm(StringBuilder text, String term) {
        if (text.length() > 0) {
            text.append(" + ");
        }
        text.append(term);
    }

    private static DamageConversion.Entry entryFor(DamageConversion conversion,
                                                   DamageCalculator.Portion portion) {
        for (DamageConversion.Entry entry : conversion.entries()) {
            if (entry.school().equals(portion.school()) && entry.mode() == portion.mode()) {
                return entry;
            }
        }
        return null;
    }

    private static Component subLine(String text) {
        return Component.literal("  " + text).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static String formatRatio(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String modeTagKey(DamageConversion.Mode mode) {
        return mode == DamageConversion.Mode.SPLIT ? SPLIT_MODE_KEY : ADDITIVE_MODE_KEY;
    }

    private static String shortSchoolName(String attributeName) {
        String trimmed = attributeName.trim();
        for (String suffix : SCHOOL_NAME_SUFFIXES) {
            if (trimmed.endsWith(suffix) && trimmed.length() > suffix.length()) {
                return trimmed.substring(0, trimmed.length() - suffix.length());
            }
        }
        return trimmed;
    }

    private static String rawSchoolName(String schoolId) {
        SpellSchool school = SchoolResolver.resolve(schoolId);
        if (school == null) {
            return schoolId;
        }
        return Component.translatable(ATTRIBUTE_NAME_PREFIX + school.id.getNamespace()
                + "." + school.id.getPath()).getString();
    }

    private static MutableComponent attributeLine(String amount, Component label, Integer colour) {
        MutableComponent line = Component.translatable(VANILLA_ATTRIBUTE_LINE_KEY, amount, label);
        return colour == null
                ? line.withStyle(ChatFormatting.AQUA)
                : line.withStyle(style -> style.withColor(colour));
    }

    private static int indexOfDamageLine(List<Component> lines, boolean ranged) {
        String key = ranged ? RANGED_DAMAGE_ATTRIBUTE_KEY : ATTACK_DAMAGE_ATTRIBUTE_KEY;
        for (int i = 0; i < lines.size(); i++) {
            if (mentionsDamageModifier(lines.get(i), key, ranged, 0)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean mentionsDamageModifier(Component component, String key,
                                                  boolean ranged, int depth) {
        if (component == null || depth > MAX_COMPONENT_DEPTH) {
            return false;
        }

        if (component.getContents() instanceof TranslatableContents contents) {
            if (isDamageLineKey(contents.getKey(), ranged)
                    && argsMentionDamage(contents, key)) {
                return true;
            }
            for (Object arg : contents.getArgs()) {
                if (arg instanceof Component nested
                        && mentionsDamageModifier(nested, key, ranged, depth + 1)) {
                    return true;
                }
            }
        }

        for (Component sibling : component.getSiblings()) {
            if (mentionsDamageModifier(sibling, key, ranged, depth + 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDamageLineKey(String key, boolean ranged) {
        if (key.startsWith(MODIFIER_EQUALS_PREFIX)) {
            return true;
        }
        return ranged && key.startsWith(MODIFIER_PLUS_PREFIX);
    }

    private static boolean argsMentionDamage(TranslatableContents contents, String key) {
        String attributeName = Component.translatable(key).getString();

        for (Object arg : contents.getArgs()) {
            if (!(arg instanceof Component component)) {
                continue;
            }
            if (component.getContents() instanceof TranslatableContents inner
                    && inner.getKey().equals(key)) {
                return true;
            }
            String rendered = IconLeading.split(component.getString()).text();
            if (!rendered.isEmpty()
                    && rendered.equalsIgnoreCase(IconLeading.split(attributeName).text())) {
                return true;
            }
        }
        return false;
    }

    private static String format(float value) {
        return value == Math.floor(value)
                ? String.valueOf((int) value)
                : String.format(Locale.ROOT, "%.1f", value);
    }
}
