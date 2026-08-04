package net.pixeldreamstudios.spw.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class SpwConfig {
    private SpwConfig() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String COMMON_FILE = "common.json";
    private static final String CLIENT_FILE = "client.json";

    private static CommonConfig common = new CommonConfig();
    private static ClientConfig client = new ClientConfig();
    private static Path directory;

    public static boolean spreadDamageNumbersOnMobs() {
        return common.spread_damage_numbers_on_mobs;
    }

    public static boolean rollOnCraft() {
        return common.roll_on_craft;
    }

    public static boolean isBlacklistedProjectile(String entityId) {
        return common.projectile_blacklist.contains(entityId);
    }

    public static String particleFor(String schoolId) {
        String id = client.school_particles.get(schoolId);
        return id == null ? "" : id;
    }

    public static boolean showModeTags() {
        return client.show_mode_tags;
    }

    public static boolean showDamageBreakdown() {
        return client.show_damage_breakdown;
    }

    public static void setShowModeTags(boolean value) {
        client.show_mode_tags = value;
    }

    public static void setShowDamageBreakdown(boolean value) {
        client.show_damage_breakdown = value;
    }

    public static void setSpreadDamageNumbersOnMobs(boolean value) {
        common.spread_damage_numbers_on_mobs = value;
    }

    public static void setParticleFor(String schoolId, String particleId) {
        client.school_particles.put(schoolId, particleId);
    }

    public static Map<String, String> schoolParticles() {
        return new LinkedHashMap<>(client.school_particles);
    }

    public static List<String> projectileBlacklist() {
        return new ArrayList<>(common.projectile_blacklist);
    }

    public static void setProjectileBlacklist(List<String> ids) {
        common.projectile_blacklist = new ArrayList<>(ids);
    }

    public static boolean schoolAllowsSplit(String schoolId, boolean archetypeDefault) {
        if (common.additive_only_schools.contains(schoolId)) {
            return false;
        }
        if (common.split_and_additive_schools.contains(schoolId)) {
            return true;
        }
        return archetypeDefault;
    }

    public static List<String> splitAndAdditiveSchools() {
        return new ArrayList<>(common.split_and_additive_schools);
    }

    public static void setSplitAndAdditiveSchools(List<String> ids) {
        common.split_and_additive_schools = new ArrayList<>(ids);
    }

    public static List<String> additiveOnlySchools() {
        return new ArrayList<>(common.additive_only_schools);
    }

    public static void setAdditiveOnlySchools(List<String> ids) {
        common.additive_only_schools = new ArrayList<>(ids);
    }

    public static void save() {
        if (directory == null) {
            return;
        }
        write(directory.resolve(COMMON_FILE), common);
        write(directory.resolve(CLIENT_FILE), client);
    }

    public static void loadCommon(Path configDirectory) {
        directory = directory(configDirectory);
        common = read(directory.resolve(COMMON_FILE), CommonConfig.class, CommonConfig::new);
    }

    public static void loadClient(Path configDirectory, Map<String, String> defaults) {
        directory = directory(configDirectory);
        Path file = directory.resolve(CLIENT_FILE);
        ClientConfig loaded = read(file, ClientConfig.class, ClientConfig::new);

        boolean changed = false;
        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            if (!loaded.school_particles.containsKey(entry.getKey())) {
                loaded.school_particles.put(entry.getKey(), entry.getValue());
                changed = true;
            }
        }

        client = loaded;
        if (changed || !Files.exists(file)) {
            write(file, loaded);
        }
    }

    private static Path directory(Path configDirectory) {
        return configDirectory.resolve(SpellPoweredWeapons.MOD_ID);
    }

    private static <T> T read(Path file, Class<T> type, Supplier<T> fallback) {
        try {
            if (Files.exists(file)) {
                T parsed = GSON.fromJson(Files.readString(file), type);
                if (parsed != null) {
                    return parsed;
                }
            } else {
                Files.createDirectories(file.getParent());
                write(file, fallback.get());
            }
        } catch (Exception e) {
            SpellPoweredWeapons.LOGGER.warn("Could not read {}, using defaults.", file, e);
        }
        return fallback.get();
    }

    private static void write(Path file, Object value) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(value));
        } catch (Exception e) {
            SpellPoweredWeapons.LOGGER.warn("Could not write {}.", file, e);
        }
    }
}
