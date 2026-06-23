package de.dennisthegamer.shulkersort.keybind;

import de.dennisthegamer.shulkersort.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersort.sort.ShulkerSortEngine;
import de.dennisthegamer.shulkersort.sort.SortResult;
import de.dennisthegamer.shulkersort.network.SortRequestPayload;
import de.dennisthegamer.shulkersort.undo.SortUndoManager;
import de.dennisthegamer.shulkersort.util.NotificationHelper;
import de.dennisthegamer.shulkersort.util.ShulkerBoxHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class SortKeybindHandler {
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("shulkersort", "shulkersort"));

    private static KeyBinding sortKeybind;

    public static void register() {
        sortKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shulkersort.sort",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SortKeybindHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        while (sortKeybind.wasPressed()) {
            if (client.player == null) continue;
            if (client.currentScreen != null) continue; // Don't trigger when a screen is open

            boolean shiftHeld = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                    || InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (shiftHeld) {
                handleUndo(client);
                continue;
            }

            MinecraftServer integratedServer = client.getServer();
            if (integratedServer != null) {
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortOnServer(client, integratedServer);
            } else if (ClientPlayNetworking.canSend(SortRequestPayload.TYPE)) {
                SortingHudOverlay.show();
                ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.SORT));
            } else if (client.interactionManager != null && client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortClientAndSyncCreative(client);
            } else {
                NotificationHelper.sendError(client.player, "shulkersort.message.error.multiplayer_no_mod");
            }
        }
    }

    private static void handleUndo(MinecraftClient client) {
        MinecraftServer integratedServer = client.getServer();
        if (integratedServer != null) {
            undoOnServer(client, integratedServer);
        } else if (ClientPlayNetworking.canSend(SortRequestPayload.TYPE)) {
            ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.UNDO));
        } else if (client.interactionManager != null && client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
            undoClientAndSyncCreative(client);
        } else {
            NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_nothing");
        }
    }

    private static void sortOnServer(MinecraftClient client, MinecraftServer server) {
        SortingHudOverlay.show();
        UUID playerUUID = client.player.getUuid();

        server.execute(() -> {
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            SortResult result = ShulkerSortEngine.sort(serverPlayer.getInventory());
            serverPlayer.playerScreenHandler.sendContentUpdates();

            if (result.success()) {
                NotificationHelper.sendSuccess(serverPlayer, result.boxesSorted(), result.itemsMoved());
            } else {
                MinecraftClient.getInstance().execute(() -> SortUndoManager.get().clear());
                NotificationHelper.sendError(serverPlayer, result.errorMessage());
            }
        });
    }

    private static void sortClientAndSyncCreative(MinecraftClient client) {
        SortingHudOverlay.show();

        SortResult result = ShulkerSortEngine.sort(client.player.getInventory());

        if (result.success()) {
            ClientPlayerInteractionManager interactionManager = client.interactionManager;
            for (int i = 0; i < 36; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (ShulkerBoxHelper.isShulkerBox(stack)) {
                    int containerSlot = i < 9 ? i + 36 : i;
                    interactionManager.clickCreativeStack(stack.copy(), containerSlot);
                }
            }
            NotificationHelper.sendSuccess(client.player, result.boxesSorted(), result.itemsMoved());
        } else {
            SortUndoManager.get().clear();
            NotificationHelper.sendError(client.player, result.errorMessage());
        }
    }

    private static void undoOnServer(MinecraftClient client, MinecraftServer server) {
        UUID playerUUID = client.player.getUuid();
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_nothing");
            return;
        }

        server.execute(() -> {
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            for (int i = 0; i < 36; i++) {
                serverPlayer.getInventory().setStack(i, snapshot.get(i).copy());
            }
            serverPlayer.playerScreenHandler.sendContentUpdates();
            NotificationHelper.sendInfo(serverPlayer, "shulkersort.message.undo_success");
        });
    }

    private static void undoClientAndSyncCreative(MinecraftClient client) {
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_nothing");
            return;
        }

        for (int i = 0; i < 36; i++) {
            client.player.getInventory().setStack(i, snapshot.get(i).copy());
        }
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = snapshot.get(i);
            if (!stack.isEmpty()) {
                int containerSlot = i < 9 ? i + 36 : i;
                interactionManager.clickCreativeStack(stack.copy(), containerSlot);
            }
        }
        NotificationHelper.sendInfo(client.player, "shulkersort.message.undo_success");
    }
}
