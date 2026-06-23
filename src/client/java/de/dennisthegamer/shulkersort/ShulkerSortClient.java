package de.dennisthegamer.shulkersort;

import de.dennisthegamer.shulkersort.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersort.keybind.SortKeybindHandler;
import de.dennisthegamer.shulkersort.tooltip.ShulkerTooltipRenderer;
import de.dennisthegamer.shulkersort.config.ShulkerSortConfig;
import net.fabricmc.api.ClientModInitializer;

public class ShulkerSortClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ShulkerSortConfig.getInstance();
        SortKeybindHandler.register();
        ShulkerTooltipRenderer.register();
        SortingHudOverlay.register();

        ShulkerSort.LOGGER.info("ShulkerSort client initialized!");
    }
}
