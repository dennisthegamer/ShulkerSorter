package com.shulkersort.keybind;

import com.shulkersort.hud.SortingHudOverlay;
import com.shulkersort.sort.ShulkerSortEngine;
import com.shulkersort.sort.SortResult;
import com.shulkersort.util.NotificationHelper;
import com.shulkersort.util.ShulkerBoxHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

            MinecraftServer integratedServer = client.getSingleplayerServer();
            if (integratedServer != null) {
                // Singleplayer (creative + survival): sort on server-side inventory
                sortOnServer(client, integratedServer);
            } else if (client.gameMode != null && client.gameMode.getPlayerMode() == GameType.CREATIVE) {
                // Multiplayer creative: sort client-side and sync via creative packets
                sortClientAndSyncCreative(client);
            } else {
                // Multiplayer survival: not supported without server-side mod
                NotificationHelper.sendError(client.player, "shulkersort.message.error.multiplayer_survival");
            }
        }
    }

    private static void sortOnServer(Minecraft client, MinecraftServer server) {
        SortingHudOverlay.show();

        UUID playerUUID = client.player.getUUID();

        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            SortResult result = ShulkerSortEngine.sort(serverPlayer.getInventory());
            serverPlayer.inventoryMenu.broadcastChanges();

            // Send feedback on server thread (serverPlayer can receive messages)
            if (result.success()) {
                NotificationHelper.sendSuccess(serverPlayer, result.boxesSorted());
            } else {
                NotificationHelper.sendError(serverPlayer, result.errorMessage());
            }
        });
    }

    private static void sortClientAndSyncCreative(Minecraft client) {
        SortingHudOverlay.show();

        SortResult result = ShulkerSortEngine.sort(client.player.getInventory());

        if (result.success()) {
            // Sync modified shulker boxes to server via creative mode packets
            MultiPlayerGameMode gameMode = client.gameMode;
            for (int i = 0; i < 36; i++) {
                ItemStack stack = client.player.getInventory().getItem(i);
                if (ShulkerBoxHelper.isShulkerBox(stack)) {
                    // Convert inventory slot to container slot index
                    // Hotbar (0-8) -> container slots 36-44
                    // Main inventory (9-35) -> container slots 9-35
                    int containerSlot = i < 9 ? i + 36 : i;
                    gameMode.handleCreativeModeItemAdd(stack.copy(), containerSlot);
                }
            }
            NotificationHelper.sendSuccess(client.player, result.boxesSorted());
        } else {
            NotificationHelper.sendError(client.player, result.errorMessage());
        }
    }
}
