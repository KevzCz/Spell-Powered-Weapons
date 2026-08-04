package net.pixeldreamstudios.spw;

import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import net.pixeldreamstudios.spw.config.SpwConfig;
import net.pixeldreamstudios.spw.damage.SchoolResolver;
import net.pixeldreamstudios.spw.fx.SchoolVisuals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class SpellPoweredWeapons {
    public static final String MOD_ID = "spell_powered_weapons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private SpellPoweredWeapons() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void init() {
        Path configDir = Platform.getConfigFolder();
        SpwConfig.loadCommon(configDir);
        SpwConfig.loadClient(configDir,
                SchoolVisuals.defaults(SchoolResolver.convertibleIds()));
        SpwEvents.register();
    }
}
