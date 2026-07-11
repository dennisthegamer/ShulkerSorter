package de.dennisthegamer.shulkersorter;

import de.dennisthegamer.shulkersorter.network.ServerSortHandler;
import de.dennisthegamer.shulkersorter.network.ServerUndoManager;
import dev.architectury.event.events.common.PlayerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerSorter {
    public static final String MOD_ID = "shulkersorter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        ServerSortHandler.register();
        PlayerEvent.PLAYER_QUIT.register(player ->
                ServerUndoManager.get().clear(player.getUUID()));
        LOGGER.info("ShulkerSorter initialized!");
    }
}
