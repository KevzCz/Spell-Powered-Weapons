package net.pixeldreamstudios.spw.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.client.config.SpwConfigScreen;

@EventBusSubscriber(modid = SpellPoweredWeapons.MOD_ID, value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class SpwNeoForgeConfigScreen {
    private SpwNeoForgeConfigScreen() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModContainer container = net.neoforged.fml.ModList.get()
                .getModContainerById(SpellPoweredWeapons.MOD_ID)
                .orElseThrow();
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new SpwConfigScreen(parent));
    }
}
