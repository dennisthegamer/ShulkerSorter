package de.dennisthegamer.shulkersorter.keybind;

import de.dennisthegamer.shulkersorter.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersorter.sort.ShulkerSorterEngine;
import de.dennisthegamer.shulkersorter.sort.SortResult;
import de.dennisthegamer.shulkersorter.network.SortRequestPayload;
import de.dennisthegamer.shulkersorter.undo.SortUndoManager;
import de.dennisthegamer.shulkersorter.util.NotificationHelper;
import de.dennisthegamer.shulkersorter.util.ShulkerBoxHelper;
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
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class SortKeybindHandler {
    private static KeyBinding sortKeybind;

    public static void register() {
        sortKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shulkersorter.sort",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.shulkersorter"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SortKeybindHandler::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        while (sortKeybind.wasPressed()) {
            if (client.player == null) continue;
            if (client.currentScreen != null) continue;

            boolean shiftHeld = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
                    || InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (shiftHeld) {
                handleUndo(client);
                continue;
            }

            MinecraftServer integratedServer = client.getServer();
            if (integratedServer != null) {
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortOnServer(client, integratedServer);
            } else if (ClientPlayNetworking.canSend(SortRequestPayload.ID)) {
                SortingHudOverlay.show();
                ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.SORT));
            } else if (client.interactionManager != null && client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortClientAndSyncCreative(client);
            } else {
                NotificationHelper.sendError(client.player, "shulkersorter.message.error.multiplayer_no_mod");
            }
        }
    }

    private static void handleUndo(MinecraftClient client) {
        MinecraftServer integratedServer = client.getServer();
        if (integratedServer != null) {
            undoOnServer(client, integratedServer);
        } else if (ClientPlayNetworking.canSend(SortRequestPayload.ID)) {
            ClientPlayNetworking.send(new SortRequestPayload(SortRequestPayload.Action.UNDO));
        } else if (client.interactionManager != null && client.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
            undoClientAndSyncCreative(client);
        } else {
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
        }
    }

    private static void sortOnServer(MinecraftClient client, MinecraftServer server) {
        SortingHudOverlay.show();
        UUID playerUUID = client.player.getUuid();

        server.execute(() -> {
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            SortResult result = ShulkerSorterEngine.sort(serverPlayer.getInventory());
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

        SortResult result = ShulkerSorterEngine.sort(client.player.getInventory());

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
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
            return;
        }

        server.execute(() -> {
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            for (int i = 0; i < 36; i++) {
                serverPlayer.getInventory().setStack(i, snapshot.get(i).copy());
            }
            serverPlayer.playerScreenHandler.sendContentUpdates();
            NotificationHelper.sendInfo(serverPlayer, "shulkersorter.message.undo_success");
        });
    }

    private static void undoClientAndSyncCreative(MinecraftClient client) {
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
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
        NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_success");
    }
}
