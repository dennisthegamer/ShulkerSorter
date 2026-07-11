package de.dennisthegamer.shulkersorter;

import de.dennisthegamer.shulkersorter.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersorter.keybind.SortKeybindHandler;
import de.dennisthegamer.shulkersorter.tooltip.ShulkerTooltipRenderer;
import de.dennisthegamer.shulkersorter.config.ShulkerSorterConfig;
import de.dennisthegamer.shulkersorter.network.ClientSortHandler;
import de.dennisthegamer.shulkersorter.undo.SortUndoManager;
import dev.architectury.event.events.client.ClientPlayerEvent;

public class ShulkerSorterClient {

    public static void init() {
        ShulkerSorterConfig.getInstance();
        SortKeybindHandler.register();
        ShulkerTooltipRenderer.register();
        SortingHudOverlay.register();
        ClientSortHandler.register();
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> SortUndoManager.get().clear());

        ShulkerSorter.LOGGER.info("ShulkerSorter client initialized!");
    }
}
