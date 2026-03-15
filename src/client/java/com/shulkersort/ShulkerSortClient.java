package com.shulkersort;

import com.shulkersort.hud.SortingHudOverlay;
import com.shulkersort.keybind.SortKeybindHandler;
import com.shulkersort.tooltip.ShulkerTooltipRenderer;
import com.shulkersort.config.ShulkerSortConfig;
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
