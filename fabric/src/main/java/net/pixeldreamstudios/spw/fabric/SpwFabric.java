package net.pixeldreamstudios.spw.fabric;

import net.fabricmc.api.ModInitializer;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.component.SpwComponents;

public final class SpwFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SpwComponents.register();
        SpellPoweredWeapons.init();
    }
}
