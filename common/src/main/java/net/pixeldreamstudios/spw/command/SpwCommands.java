package net.pixeldreamstudios.spw.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.component.Conversions;
import net.pixeldreamstudios.spw.component.DamageConversion;
import net.pixeldreamstudios.spw.component.SpwComponents;
import net.pixeldreamstudios.spw.damage.PhysicalReduction;
import net.pixeldreamstudios.spw.damage.SchoolResolver;
import net.pixeldreamstudios.spw.roll.AuthoredConversion;
import net.pixeldreamstudios.spw.roll.RollConfig;
import net.pixeldreamstudios.spw.roll.RollTables;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class SpwCommands {
    private SpwCommands() {}

    private static final SuggestionProvider<CommandSourceStack> SCHOOLS = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    SpellSchools.all().stream()
                            .filter(SchoolResolver::isConvertible)
                            .map(school -> school.id.toString())
                            .toList(),
                    builder);

    private static final SuggestionProvider<CommandSourceStack> DAMAGE_TYPES = (context, builder) ->
            SharedSuggestionProvider.suggestResource(
                    context.getSource().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .keySet(),
                    builder);

    private static final SuggestionProvider<CommandSourceStack> NAME_FORMAT = (context, builder) ->
            SharedSuggestionProvider.suggest(
                    List.of("\"Fire\"", "\"key:attribute.name.spell_power.fire\""),
                    builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(root("spw"));
        dispatcher.register(root("spell_powered_weapons"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(String name) {
        return Commands.literal(name)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("split")
                        .then(Commands.argument("school", ResourceLocationArgument.id())
                                .suggests(SCHOOLS)
                                .then(Commands.argument("ratio", FloatArgumentType.floatArg(0f, 1f))
                                        .then(Commands.argument("coefficient", FloatArgumentType.floatArg(0f))
                                                .executes(context -> add(context,
                                                        DamageConversion.Mode.SPLIT,
                                                        FloatArgumentType.getFloat(context, "ratio")))
                                                .then(Commands.argument("base", FloatArgumentType.floatArg(0f))
                                                        .executes(context -> add(context,
                                                                DamageConversion.Mode.SPLIT,
                                                                FloatArgumentType.getFloat(context, "ratio"))))))))
                .then(Commands.literal("additive")
                        .then(Commands.argument("school", ResourceLocationArgument.id())
                                .suggests(SCHOOLS)
                                .then(Commands.argument("coefficient", FloatArgumentType.floatArg(0f))
                                        .executes(context -> add(context,
                                                DamageConversion.Mode.ADDITIVE, 0f))
                                        .then(Commands.argument("base", FloatArgumentType.floatArg(0f))
                                                .executes(context -> add(context,
                                                        DamageConversion.Mode.ADDITIVE, 0f))))))
                .then(Commands.literal("full_elemental")
                        .then(Commands.argument("school", ResourceLocationArgument.id())
                                .suggests(SCHOOLS)
                                .then(Commands.argument("coefficient", FloatArgumentType.floatArg(0f))
                                        .executes(context -> add(context,
                                                DamageConversion.Mode.SPLIT, 1f))
                                        .then(Commands.argument("base", FloatArgumentType.floatArg(0f))
                                                .executes(context -> add(context,
                                                        DamageConversion.Mode.SPLIT, 1f))))))
                .then(Commands.literal("proportional")
                        .then(Commands.argument("share", FloatArgumentType.floatArg(0f, 1f))
                                .then(Commands.literal("of")
                                        .then(Commands.argument("source_type", ResourceLocationArgument.id())
                                                .suggests(DAMAGE_TYPES)
                                                .then(Commands.literal("as")
                                                        .then(Commands.argument("output_type", ResourceLocationArgument.id())
                                                                .suggests(DAMAGE_TYPES)
                                                                .executes(SpwCommands::addProportional)
                                                                .then(Commands.argument("source_name", StringArgumentType.string())
                                                                        .suggests(NAME_FORMAT)
                                                                        .executes(SpwCommands::addProportional)
                                                                        .then(Commands.argument("output_name", StringArgumentType.string())
                                                                                .suggests(NAME_FORMAT)
                                                                                .executes(SpwCommands::addProportional)))))))
                                .then(Commands.literal("as")
                                        .then(Commands.argument("output_type", ResourceLocationArgument.id())
                                                .suggests(DAMAGE_TYPES)
                                                .executes(SpwCommands::addProportional)
                                                .then(Commands.argument("output_name", StringArgumentType.string())
                                                        .suggests(NAME_FORMAT)
                                                        .executes(SpwCommands::addProportional))))))
                .then(Commands.literal("info").executes(SpwCommands::info))
                .then(Commands.literal("remove")
                        .then(Commands.argument("school", ResourceLocationArgument.id())
                                .suggests(SCHOOLS)
                                .executes(SpwCommands::remove)))
                .then(Commands.literal("clear").executes(SpwCommands::clear))
                .then(Commands.literal("roll")
                        .then(Commands.literal("simulate")
                                .executes(ctx -> simulate(ctx, 1000))
                                .then(Commands.argument("samples", IntegerArgumentType.integer(1, 100000))
                                        .executes(ctx -> simulate(ctx,
                                                IntegerArgumentType.getInteger(ctx, "samples"))))))
                .then(Commands.literal("hide")
                        .then(Commands.literal("on").executes(ctx -> flag(ctx,
                                SpwComponents.HIDE_DAMAGE_LINE, true, "Damage line hidden.")))
                        .then(Commands.literal("off").executes(ctx -> flag(ctx,
                                SpwComponents.HIDE_DAMAGE_LINE, false, "Damage line shown."))))
                .then(Commands.literal("suppress")
                        .then(Commands.literal("on").executes(ctx -> flag(ctx,
                                SpwComponents.SUPPRESS_PHYSICAL, true,
                                "Physical damage suppressed — this weapon now deals only elemental damage.")))
                        .then(Commands.literal("off").executes(ctx -> flag(ctx,
                                SpwComponents.SUPPRESS_PHYSICAL, false,
                                "Physical damage restored."))))
                .then(Commands.literal("debug")
                        .then(Commands.literal("on").executes(ctx -> debug(ctx, true)))
                        .then(Commands.literal("off").executes(ctx -> debug(ctx, false))));
    }

    private static boolean debugEnabled = false;

    public static boolean debugEnabled() {
        return debugEnabled;
    }

    private static int debug(CommandContext<CommandSourceStack> context, boolean on) {
        debugEnabled = on;
        context.getSource().sendSuccess(() -> Component.literal(
                "Split-reduction debug " + (on ? "enabled" : "disabled") + ".")
                .withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int flag(CommandContext<CommandSourceStack> context,
                            DataComponentType<Boolean> type,
                            boolean on, String message) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return fail(context, "Hold the weapon you want to modify.");
        }

        if (on) {
            stack.set(type, Boolean.TRUE);
        } else {
            stack.remove(type);
        }
        context.getSource().sendSuccess(() -> Component.literal(message)
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int addProportional(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return fail(context, "Hold the weapon you want to modify.");
        }

        float share = FloatArgumentType.getFloat(context, "share");
        String outputType = ResourceLocationArgument.getId(context, "output_type").toString();
        String sourceType = optionalId(context, "source_type");

        DamageConversion current = stack.getOrDefault(
                SpwComponents.DAMAGE_CONVERSION, DamageConversion.EMPTY);
        DamageConversion updated = current.with(DamageConversion.Entry.proportional(
                share,
                Optional.of(outputType),
                Optional.ofNullable(sourceType),
                nameArg(context, "output_name"),
                nameArg(context, "source_name"),
                Optional.empty()));
        stack.set(SpwComponents.DAMAGE_CONVERSION, updated);
        applyReduction(context, stack, updated.physicalFraction());

        String from = sourceType == null ? "damage dealt" : sourceType;
        context.getSource().sendSuccess(() -> Component.literal(
                "Added proportional " + percent(share) + " of " + from + " as " + outputType)
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static String optionalId(CommandContext<CommandSourceStack> context, String name) {
        try {
            return ResourceLocationArgument.getId(context, name).toString();
        } catch (IllegalArgumentException absent) {
            return null;
        }
    }

    private static Optional<Component> nameArg(CommandContext<CommandSourceStack> context, String name) {
        String raw;
        try {
            raw = StringArgumentType.getString(context, name);
        } catch (IllegalArgumentException absent) {
            return Optional.empty();
        }
        if (raw.startsWith(KEY_PREFIX)) {
            return Optional.of(Component.translatable(raw.substring(KEY_PREFIX.length())));
        }
        if (raw.startsWith(TEXT_PREFIX)) {
            return Optional.of(Component.literal(raw.substring(TEXT_PREFIX.length())));
        }
        return Optional.of(Component.literal(raw));
    }

    private static final String KEY_PREFIX = "key:";
    private static final String TEXT_PREFIX = "text:";

    private static int add(CommandContext<CommandSourceStack> context,
                           DamageConversion.Mode mode, float ratio) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return fail(context, "Hold the weapon you want to modify.");
        }

        String schoolId = ResourceLocationArgument.getId(context, "school").toString();
        SpellSchool school = SchoolResolver.resolve(schoolId);
        if (school == null) {
            return fail(context, "Unknown spell school: " + schoolId);
        }
        if (!SchoolResolver.isConvertible(school)) {
            return fail(context, schoolId + " is a " + school.archetype
                    + " school, not MAGIC — weapon damage cannot be converted into it.");
        }

        float coefficient = FloatArgumentType.getFloat(context, "coefficient");

        float base;
        try {
            base = FloatArgumentType.getFloat(context, "base");
        } catch (IllegalArgumentException absent) {
            base = 0f;
        }

        DamageConversion current = stack.getOrDefault(
                SpwComponents.DAMAGE_CONVERSION, DamageConversion.EMPTY);
        DamageConversion updated = current.with(
                new DamageConversion.Entry(mode, schoolId, ratio, base, coefficient));
        stack.set(SpwComponents.DAMAGE_CONVERSION, updated);
        applyReduction(context, stack, updated.physicalFraction());

        if (updated.physicalFraction() <= 0f) {
            stack.set(SpwComponents.SUPPRESS_PHYSICAL, Boolean.TRUE);
            stack.set(SpwComponents.HIDE_DAMAGE_LINE, Boolean.TRUE);
        }

        float reportedBase = base;
        context.getSource().sendSuccess(() -> Component.literal(
                "Added " + mode.name().toLowerCase(Locale.ROOT) + " " + schoolId
                        + (mode == DamageConversion.Mode.SPLIT ? " ratio " + ratio : "")
                        + (reportedBase > 0f ? " base " + reportedBase : "")
                        + " coefficient " + coefficient).withStyle(ChatFormatting.GREEN), false);

        warnIfOversubscribed(context, updated);
        return 1;
    }

    private static void warnIfOversubscribed(CommandContext<CommandSourceStack> context,
                                             DamageConversion conversion) {
        if (conversion.rawSplitTotal() <= 1f) {
            return;
        }
        List<String> shares = new ArrayList<>();
        for (DamageConversion.Entry entry : conversion.entries()) {
            if (entry.isSplit()) {
                shares.add(entry.school() + " "
                        + percent(conversion.effectiveRatio(entry)));
            }
        }
        context.getSource().sendSuccess(() -> Component.literal(
                "Split ratios total " + percent(conversion.rawSplitTotal())
                        + " — normalized to shares: " + String.join(", ", shares)
                        + ". No physical damage remains.").withStyle(ChatFormatting.YELLOW), false);
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        DamageConversion conversion = stack.get(SpwComponents.DAMAGE_CONVERSION);

        if (conversion == null || conversion.isEmpty()) {
            context.getSource().sendSuccess(
                    () -> Component.literal("No damage conversion on this item."), false);
            return 0;
        }

        boolean suppressed = Conversions.suppressesPhysical(stack);
        context.getSource().sendSuccess(() -> Component.literal(
                "Physical: " + (suppressed
                        ? "suppressed"
                        : percent(conversion.physicalFraction())))
                .withStyle(ChatFormatting.WHITE), false);
        if (Boolean.TRUE.equals(stack.get(SpwComponents.HIDE_DAMAGE_LINE)) && !suppressed) {
            context.getSource().sendSuccess(() -> Component.literal("Damage line: hidden")
                    .withStyle(ChatFormatting.WHITE), false);
        }

        for (DamageConversion.Entry entry : conversion.entries()) {
            if (entry.mode() == DamageConversion.Mode.PROPORTIONAL) {
                context.getSource().sendSuccess(() -> Component.literal(
                        "  proportional — " + percent(entry.ratio()) + " of ")
                        .append(entry.sourceDisplay())
                        .append(" as ")
                        .append(entry.outputDisplay())
                        .withStyle(ChatFormatting.GRAY), false);
                continue;
            }

            boolean resolvable = SchoolResolver.resolve(entry.school()) != null;
            String detail = entry.isSplit()
                    ? "split " + percent(conversion.effectiveRatio(entry))
                    : "additive";
            context.getSource().sendSuccess(() -> Component.literal(
                    "  " + entry.school() + " — " + detail
                            + (entry.base() > 0f ? ", base " + entry.base() : "")
                            + ", coefficient " + entry.coefficient()
                            + (resolvable ? "" : "  [unresolved]"))
                    .withStyle(resolvable ? ChatFormatting.GRAY : ChatFormatting.RED), false);
        }
        return conversion.entries().size();
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        DamageConversion conversion = stack.get(SpwComponents.DAMAGE_CONVERSION);
        if (conversion == null || conversion.isEmpty()) {
            return fail(context, "No damage conversion on this item.");
        }

        String schoolId = ResourceLocationArgument.getId(context, "school").toString();
        DamageConversion updated = conversion.without(schoolId);
        if (updated.entries().size() == conversion.entries().size()) {
            return fail(context, "No entry for " + schoolId + " on this item.");
        }

        if (updated.isEmpty()) {
            stack.remove(SpwComponents.DAMAGE_CONVERSION);
        } else {
            stack.set(SpwComponents.DAMAGE_CONVERSION, updated);
        }
        applyReduction(context, stack, updated.physicalFraction());
        context.getSource().sendSuccess(() -> Component.literal("Removed " + schoolId + ".")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int simulate(CommandContext<CommandSourceStack> context, int samples)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return fail(context, "Hold the item you want to simulate rolls for.");
        }

        AuthoredConversion authored = RollTables.authoredFor(stack);
        if (authored != null) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "This item has an authored conversion (mode "
                            + authored.mode().name().toLowerCase(Locale.ROOT)
                            + ") — it always applies, no roll.").withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }

        List<RollConfig> pool = RollTables.configsFor(stack);
        if (pool.isEmpty()) {
            return fail(context, "No roll configs match this item.");
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        RandomSource random = RandomSource.create();
        for (int i = 0; i < samples; i++) {
            RollConfig config = RollTables.pick(stack, random);
            String key = config == null
                    ? "(none)"
                    : config.mode().name().toLowerCase(Locale.ROOT) + "  " + config.id();
            counts.merge(key, 1, Integer::sum);
        }

        context.getSource().sendSuccess(() -> Component.literal(
                "Simulated " + samples + " rolls on " + stack.getHoverName().getString() + ":")
                .withStyle(ChatFormatting.WHITE), false);
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    int pct = Math.round(e.getValue() * 100f / samples);
                    context.getSource().sendSuccess(() -> Component.literal(
                            "  " + pct + "%  (" + e.getValue() + ")  " + e.getKey())
                            .withStyle(ChatFormatting.GRAY), false);
                });
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.get(SpwComponents.DAMAGE_CONVERSION) == null
                && stack.get(SpwComponents.HIDE_DAMAGE_LINE) == null
                && stack.get(SpwComponents.SUPPRESS_PHYSICAL) == null) {
            return fail(context, "No damage conversion on this item.");
        }
        stack.remove(SpwComponents.DAMAGE_CONVERSION);
        stack.remove(SpwComponents.HIDE_DAMAGE_LINE);
        stack.remove(SpwComponents.SUPPRESS_PHYSICAL);
        applyReduction(context, stack, 1f);
        context.getSource().sendSuccess(() -> Component.literal(
                "Cleared damage conversion.").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static void applyReduction(CommandContext<CommandSourceStack> context, ItemStack stack,
                                       float physicalFraction) {
        PhysicalReduction.debug = debugEnabled
                ? msg -> context.getSource().sendSystemMessage(Component.literal(msg))
                : null;
        try {
            PhysicalReduction.apply(stack, physicalFraction);
        } finally {
            PhysicalReduction.debug = null;
        }
    }

    private static int fail(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(Component.literal(message));
        return 0;
    }

    private static String percent(float value) {
        return Math.round(value * 100f) + "%";
    }
}
