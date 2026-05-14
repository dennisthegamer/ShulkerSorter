package com.shulkersort;

import com.shulkersort.hud.SortingHudOverlay;
import com.shulkersort.keybind.SortKeybindHandler;
import com.shulkersort.tooltip.ShulkerTooltipRenderer;
import com.shulkersort.config.ShulkerSortConfig;
import com.shulkersort.undo.SortUndoManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ShulkerSortClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ShulkerSortConfig.getInstance();
        SortKeybindHandler.register();
        ShulkerTooltipRenderer.register();
        SortingHudOverlay.register();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SortUndoManager.get().clear());

        ShulkerSort.LOGGER.info("ShulkerSort client initialized!");
    }
}
