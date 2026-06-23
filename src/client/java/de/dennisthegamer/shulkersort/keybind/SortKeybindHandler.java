package de.dennisthegamer.shulkersort.keybind;

import de.dennisthegamer.shulkersort.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersort.sort.ShulkerSortEngine;
import de.dennisthegamer.shulkersort.sort.SortResult;
import de.dennisthegamer.shulkersort.network.SortRequestPayload;
import de.dennisthegamer.shulkersort.undo.SortUndoManager;
import de.dennisthegamer.shulkersort.util.NotificationHelper;
import de.dennisthegamer.shulkersort.util.ShulkerBoxHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

import java.util.List;
import java.util.UUID;

public class SortKeybindHandler {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("shulkersort", "shulkersort"));

    private static KeyMapping sortKeybind;

    public static void register() {
        sortKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.shulkersort.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SortKeybindHandler::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        while (sortKeybind.consumeClick()) {
            if (client.player == null) continue;

            boolean shiftHeld = InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                    || InputConstants.isKeyDown(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (shiftHeld) {
                handleUndo(client);
                continue;
            }

            MinecraftServer integratedServer = client.getSingleplayerServer();
            if (integratedServer != null) {
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortOnServer(client, integratedServer);
            } else if (ClientPlayNetworking.canSend(SortRequestPayload.TYPE)) {
                SortingHudOverlay.show();
                ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.SORT));
            } else if (client.gameMode != null && client.gameMode.getPlayerMode() == GameType.CREATIVE) {
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortClientAndSyncCreative(client);
            } else {
                NotificationHelper.sendError(client.player, "shulkersort.message.error.multiplayer_no_mod");
            }
        }
    }

    private static void handleUndo(Minecraft client) {
        MinecraftServer integratedServer = client.getSingleplayerServer();
        if (integratedServer != null) {
            undoOnServer(client, integratedServer);
        } else if (ClientPlayNetworking.canSend(SortRequestPayload.TYPE)) {
            ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.UNDO));
        } else if (client.gameMode != null && client.gameMode.getPlayerMode() == GameType.CREATIVE) {
            undoClientAndSyncCreative(client);
        } else {
            NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_nothing");
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

            if (result.success()) {
                NotificationHelper.sendSuccess(serverPlayer, result.boxesSorted(), result.itemsMoved());
            } else {
                Minecraft.getInstance().execute(() -> SortUndoManager.get().clear());
                NotificationHelper.sendError(serverPlayer, result.errorMessage());
            }
        });
    }

    private static void sortClientAndSyncCreative(Minecraft client) {
        SortingHudOverlay.show();

        SortResult result = ShulkerSortEngine.sort(client.player.getInventory());

        if (result.success()) {
            MultiPlayerGameMode gameMode = client.gameMode;
            for (int i = 0; i < 36; i++) {
                ItemStack stack = client.player.getInventory().getItem(i);
                if (ShulkerBoxHelper.isShulkerBox(stack)) {
                    int containerSlot = i < 9 ? i + 36 : i;
                    gameMode.handleCreativeModeItemAdd(stack.copy(), containerSlot);
                }
            }
            NotificationHelper.sendSuccess(client.player, result.boxesSorted(), result.itemsMoved());
        } else {
            SortUndoManager.get().clear();
            NotificationHelper.sendError(client.player, result.errorMessage());
        }
    }

    private static void undoOnServer(Minecraft client, MinecraftServer server) {
        UUID playerUUID = client.player.getUUID();
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_nothing");
            return;
        }

        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            for (int i = 0; i < 36; i++) {
                serverPlayer.getInventory().setItem(i, snapshot.get(i).copy());
            }
            serverPlayer.inventoryMenu.broadcastChanges();
            NotificationHelper.sendInfo(serverPlayer, "shulkersort.message.undo_success");
        });
    }

    private static void undoClientAndSyncCreative(Minecraft client) {
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_nothing");
            return;
        }

        for (int i = 0; i < 36; i++) {
            client.player.getInventory().setItem(i, snapshot.get(i).copy());
        }
        MultiPlayerGameMode gameMode = client.gameMode;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = snapshot.get(i);
            if (!stack.isEmpty()) {
                int containerSlot = i < 9 ? i + 36 : i;
                gameMode.handleCreativeModeItemAdd(stack.copy(), containerSlot);
            }
        }
        NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_success");
    }
}
