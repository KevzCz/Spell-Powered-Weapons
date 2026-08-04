package net.pixeldreamstudios.spw.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.pixeldreamstudios.spw.client.DamageConversionTooltip;

public final class SpwFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, tooltipType, lines) ->
                DamageConversionTooltip.append(stack, lines));
    }
}
