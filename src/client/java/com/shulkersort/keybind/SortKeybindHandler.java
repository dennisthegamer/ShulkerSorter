package com.shulkersort.keybind;

import com.shulkersort.hud.SortingHudOverlay;
import com.shulkersort.sort.ShulkerSortEngine;
import com.shulkersort.sort.SortResult;
import com.shulkersort.util.NotificationHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class SortKeybindHandler {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("shulkersort", "shulkersort"));

    private static KeyMapping sortKeybind;

    public static void register() {
        sortKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.shulkersort.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J, // Using J to avoid conflicting with S (sneak)
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SortKeybindHandler::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        while (sortKeybind.consumeClick()) {
            if (client.player == null) continue;
            if (client.screen != null) continue; // Don't trigger when a screen is open

            // Show HUD overlay
            SortingHudOverlay.show();

            // Execute sorting
            SortResult result = ShulkerSortEngine.sort(client.player.getInventory());

            if (result.success()) {
                NotificationHelper.sendSuccess(client.player, result.boxesSorted());
            } else {
                NotificationHelper.sendError(client.player, result.errorMessage());
            }
        }
    }
}
