package net.pixeldreamstudios.spw.roll;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.damage.SchoolResolver;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RollTables {
    private RollTables() {}

    public static final String DIRECTORY = "spw_rolls";
    public static final String AUTHORED_DIRECTORY = "spw_conversions";

    static final String ROLL_ID_NAMESPACE = "spw";
    private static final String JSON_SUFFIX = ".json";
    private static final String TYPE_FIELD = "type";
    private static final String OVERRIDE_TYPE = "override";

    public static final TagKey<Item> EXCLUDED =
            TagKey.create(Registries.ITEM, SpellPoweredWeapons.id("roll_excluded"));

    private static List<RollConfig> configs = List.of();
    private static List<RollOverride> overrides = List.of();
    private static List<AuthoredConversion> authored = List.of();

    public static void reload(ResourceManager manager) {
        List<RollConfig> loadedConfigs = new ArrayList<>();
        List<RollOverride> loadedOverrides = new ArrayList<>();

        Map<ResourceLocation, Resource> resources =
                manager.listResources(DIRECTORY, path -> path.getPath().endsWith(JSON_SUFFIX));

        for (Map.Entry<ResourceLocation, Resource> resource : resources.entrySet()) {
            ResourceLocation file = resource.getKey();
            String relative = pathUnderDirectory(file);
            if (relative == null) {
                continue;
            }

            try (BufferedReader reader = resource.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                if (isOverride(json)) {
                    readOverride(json, file, loadedOverrides);
                } else {
                    readConfig(json, file, relative, loadedConfigs);
                }
            } catch (Exception e) {
                SpellPoweredWeapons.LOGGER.warn("Failed to read spw_rolls file {}", file, e);
            }
        }

        configs = List.copyOf(loadedConfigs);
        overrides = List.copyOf(loadedOverrides);
        authored = loadAuthored(manager);
        SpellPoweredWeapons.LOGGER.info(
                "Loaded {} roll config(s), {} override(s), {} authored conversion(s).",
                configs.size(), overrides.size(), authored.size());
    }

    private static List<AuthoredConversion> loadAuthored(ResourceManager manager) {
        List<AuthoredConversion> out = new ArrayList<>();
        Map<ResourceLocation, Resource> files =
                manager.listResources(AUTHORED_DIRECTORY, path -> path.getPath().endsWith(JSON_SUFFIX));
        for (Map.Entry<ResourceLocation, Resource> file : files.entrySet()) {
            try (BufferedReader reader = file.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                AuthoredConversion.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> SpellPoweredWeapons.LOGGER.warn(
                                "Invalid authored conversion {}: {}", file.getKey(), error))
                        .ifPresent(out::add);
            } catch (Exception e) {
                SpellPoweredWeapons.LOGGER.warn("Failed to read authored conversion {}", file.getKey(), e);
            }
        }
        return List.copyOf(out);
    }

    public static AuthoredConversion authoredFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        AuthoredConversion match = null;
        for (AuthoredConversion candidate : authored) {
            if (candidate.matches(stack)) {
                match = candidate;
            }
        }
        return match;
    }

    private static boolean isOverride(JsonElement json) {
        return json.isJsonObject()
                && json.getAsJsonObject().has(TYPE_FIELD)
                && OVERRIDE_TYPE.equalsIgnoreCase(json.getAsJsonObject().get(TYPE_FIELD).getAsString());
    }

    private static void readConfig(JsonElement json, ResourceLocation file, String relative,
                                   List<RollConfig> out) {
        RollConfig.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> SpellPoweredWeapons.LOGGER.warn(
                        "Invalid roll config {}: {}", file, error))
                .ifPresent(parsed -> {
                    RollConfig config = parsed.withId(rollId(relative));
                    warnUnknownSchools(config, file);
                    out.add(config);
                });
    }

    private static void readOverride(JsonElement json, ResourceLocation file, List<RollOverride> out) {
        RollOverride.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> SpellPoweredWeapons.LOGGER.warn(
                        "Invalid override {}: {}", file, error))
                .ifPresent(out::add);
    }

    private static void warnUnknownSchools(RollConfig config, ResourceLocation file) {
        for (RollEntry entry : config.entries()) {
            if (entry.schools().isEmpty()) {
                continue;
            }
            for (String school : entry.schools().get().declared()) {
                if (SchoolResolver.resolve(school) == null) {
                    SpellPoweredWeapons.LOGGER.warn(
                            "Roll file {} references unknown spell school '{}' — that choice is skipped at "
                                    + "roll time. Its defining mod may not be installed.", file, school);
                }
            }
        }
    }

    private static String pathUnderDirectory(ResourceLocation file) {
        String prefix = DIRECTORY + "/";
        String path = file.getPath();
        if (!path.startsWith(prefix) || !path.endsWith(JSON_SUFFIX)) {
            return null;
        }
        return path.substring(prefix.length(), path.length() - JSON_SUFFIX.length());
    }

    private static ResourceLocation rollId(String relative) {
        return ResourceLocation.fromNamespaceAndPath(ROLL_ID_NAMESPACE, relative);
    }

    public static List<RollConfig> configsFor(ItemStack stack) {
        if (stack.isEmpty() || stack.is(EXCLUDED)) {
            return List.of();
        }

        List<RollOverride> applicable = new ArrayList<>();
        boolean anyReplace = false;
        for (RollOverride override : overrides) {
            if (override.matches(stack)) {
                applicable.add(override);
                anyReplace |= override.replace();
            }
        }

        List<RollConfig> pool = new ArrayList<>();
        for (RollConfig config : configs) {
            if (!config.matches(stack)) {
                continue;
            }
            if (allowed(config, applicable, anyReplace)) {
                pool.add(config);
            }
        }
        return pool;
    }

    private static boolean allowed(RollConfig roll, List<RollOverride> applicable, boolean anyReplace) {
        boolean excluded = false;
        boolean reincluded = false;
        boolean replacePermits = !anyReplace;

        for (RollOverride override : applicable) {
            if (RollOverride.matchesAny(override.exclude(), roll)) {
                excluded = true;
            }
            if (override.reincludes(roll)) {
                reincluded = true;
            }
            if (override.replace() && RollOverride.matchesAny(override.include(), roll)) {
                replacePermits = true;
            }
        }

        if (excluded && !reincluded) {
            return false;
        }
        return replacePermits;
    }

    public static RollConfig pick(ItemStack stack, RandomSource random) {
        List<RollConfig> pool = configsFor(stack);
        if (pool.isEmpty()) {
            return null;
        }
        if (pool.size() == 1) {
            return pool.get(0);
        }

        int total = 0;
        for (RollConfig config : pool) {
            total += Math.max(1, config.weight());
        }
        int target = random.nextInt(Math.max(1, total));
        for (RollConfig config : pool) {
            target -= Math.max(1, config.weight());
            if (target < 0) {
                return config;
            }
        }
        return pool.get(pool.size() - 1);
    }

    public static boolean isEmpty() {
        return configs.isEmpty();
    }
}
