package net.pixeldreamstudios.spw.fabric;

import net.fabricmc.api.ModInitializer;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.component.SpwComponents;
import net.pixeldreamstudios.spw.enchantment.SpwEnchantmentEffects;

public final class SpwFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SpwComponents.register();
        SpwEnchantmentEffects.register();
        SpellPoweredWeapons.init();
    }
}
