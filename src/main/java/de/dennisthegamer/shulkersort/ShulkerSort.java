package de.dennisthegamer.shulkersort;

import de.dennisthegamer.shulkersort.network.ServerSortHandler;
import de.dennisthegamer.shulkersort.network.ServerUndoManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerSort implements ModInitializer {
    public static final String MOD_ID = "shulkersort";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerSortHandler.register();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ServerUndoManager.get().clear(handler.player.getUuid()));
        LOGGER.info("ShulkerSort initialized!");
    }
}
