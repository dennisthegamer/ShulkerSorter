package com.shulkersort.keybind;

import com.shulkersort.hud.SortingHudOverlay;
import com.shulkersort.sort.ShulkerSortEngine;
import com.shulkersort.sort.SortResult;
import com.shulkersort.util.NotificationHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SortKeybindHandler {
    private static KeyBinding sortKeybind;

    public static void register() {
        sortKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shulkersort.sort",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J, // Using J to avoid conflicting with S (sneak)
                "category.shulkersort"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SortKeybindHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        while (sortKeybind.wasPressed()) {
            if (client.player == null) continue;
            if (client.currentScreen != null) continue; // Don't trigger when a screen is open

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
