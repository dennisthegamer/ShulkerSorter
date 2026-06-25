package de.dennisthegamer.shulkersorter;

import de.dennisthegamer.shulkersorter.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersorter.keybind.SortKeybindHandler;
import de.dennisthegamer.shulkersorter.tooltip.ShulkerTooltipRenderer;
import de.dennisthegamer.shulkersorter.config.ShulkerSorterConfig;
import de.dennisthegamer.shulkersorter.network.ClientSortHandler;
import de.dennisthegamer.shulkersorter.undo.SortUndoManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ShulkerSorterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ShulkerSorterConfig.getInstance();
        SortKeybindHandler.register();
        ShulkerTooltipRenderer.register();
        SortingHudOverlay.register();
        ClientSortHandler.register();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SortUndoManager.get().clear());
        ShulkerSorter.LOGGER.info("ShulkerSorter client initialized!");
    }
}
