package de.dennisthegamer.shulkersorter;

import de.dennisthegamer.shulkersorter.network.ServerSortHandler;
import de.dennisthegamer.shulkersorter.network.ServerUndoManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerSorter implements ModInitializer {
    public static final String MOD_ID = "shulkersorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerSortHandler.register();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ServerUndoManager.get().clear(handler.getPlayer().getUUID()));
        LOGGER.info("ShulkerSorter initialized!");
    }
}
