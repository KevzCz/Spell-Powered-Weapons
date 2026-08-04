package net.pixeldreamstudios.spw.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.pixeldreamstudios.spw.SpellPoweredWeapons;
import net.pixeldreamstudios.spw.client.DamageConversionTooltip;

@EventBusSubscriber(modid = SpellPoweredWeapons.MOD_ID, value = Dist.CLIENT)
public final class SpwNeoForgeClient {
    private SpwNeoForgeClient() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        DamageConversionTooltip.append(event.getItemStack(), event.getToolTip());
    }
}
