package net.pixeldreamstudios.spw.compat;

import dev.architectury.platform.Platform;

public final class ModCompat {
    private ModCompat() {}

    public static final String MORE_RPG_LIBRARY_ID = "more_rpg_classes";

    private static final boolean MORE_RPG_LIBRARY = Platform.isModLoaded(MORE_RPG_LIBRARY_ID);

    public static boolean hasMoreRpgLibrary() {
        return MORE_RPG_LIBRARY;
    }
}
