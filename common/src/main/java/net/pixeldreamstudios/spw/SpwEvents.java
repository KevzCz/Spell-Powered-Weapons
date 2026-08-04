package net.pixeldreamstudios.spw;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.server.packs.PackType;
import net.pixeldreamstudios.spw.command.SpwCommands;
import net.pixeldreamstudios.spw.roll.RollReloadListener;

public final class SpwEvents {
    private SpwEvents() {}

    private static final String ROLL_TABLES_LISTENER_ID = "roll_tables";

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
                SpwCommands.register(dispatcher));

        ReloadListenerRegistry.register(PackType.SERVER_DATA, RollReloadListener.INSTANCE,
                SpellPoweredWeapons.id(ROLL_TABLES_LISTENER_ID));
    }
}
