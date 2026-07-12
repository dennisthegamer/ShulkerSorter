package de.dennisthegamer.shulkersorter.keybind;

import de.dennisthegamer.shulkersorter.hud.SortingHudOverlay;
import de.dennisthegamer.shulkersorter.sort.ShulkerSorterEngine;
import de.dennisthegamer.shulkersorter.sort.SortResult;
import de.dennisthegamer.shulkersorter.network.SortRequestPayload;
import de.dennisthegamer.shulkersorter.undo.SortUndoManager;
import de.dennisthegamer.shulkersorter.util.NotificationHelper;
import de.dennisthegamer.shulkersorter.util.ShulkerBoxHelper;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class SortKeybindHandler {
    // Vanilla INVENTORY category: registering a custom one needs the Identifier
    // class, whose mojmap name differs between 1.21.10 (ResourceLocation) and
    // 1.21.11 (Identifier) — a direct reference crashes NeoForge on 1.21.9/1.21.10.
    // The Category constants keep their names across all three versions.
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.INVENTORY;

    private static KeyMapping sortKeybind;

    public static void register() {
        sortKeybind = new KeyMapping(
                "key.shulkersorter.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        );
        KeyMappingRegistry.register(sortKeybind);

        ClientTickEvent.CLIENT_POST.register(SortKeybindHandler::onClientTick);
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
            } else if (NetworkManager.canServerReceive(SortRequestPayload.TYPE)) {
                SortingHudOverlay.show();
                NetworkManager.sendToServer(new SortRequestPayload(SortRequestPayload.Action.SORT));
            } else if (client.gameMode != null && client.gameMode.getPlayerMode() == GameType.CREATIVE) {
                SortUndoManager.get().saveSnapshot(client.player.getInventory());
                sortClientAndSyncCreative(client);
            } else {
                NotificationHelper.sendError(client.player, "shulkersorter.message.error.multiplayer_no_mod");
            }
        }
    }

    private static void handleUndo(Minecraft client) {
        MinecraftServer integratedServer = client.getSingleplayerServer();
        if (integratedServer != null) {
            undoOnServer(client, integratedServer);
        } else if (NetworkManager.canServerReceive(SortRequestPayload.TYPE)) {
            NetworkManager.sendToServer(new SortRequestPayload(SortRequestPayload.Action.UNDO));
        } else if (client.gameMode != null && client.gameMode.getPlayerMode() == GameType.CREATIVE) {
            undoClientAndSyncCreative(client);
        } else {
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
        }
    }

    private static void sortOnServer(Minecraft client, MinecraftServer server) {
        SortingHudOverlay.show();
        UUID playerUUID = client.player.getUUID();

        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            SortResult result = ShulkerSorterEngine.sort(serverPlayer.getInventory());
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

        SortResult result = ShulkerSorterEngine.sort(client.player.getInventory());

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
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
            return;
        }

        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerUUID);
            if (serverPlayer == null) return;

            for (int i = 0; i < 36; i++) {
                serverPlayer.getInventory().setItem(i, snapshot.get(i).copy());
            }
            serverPlayer.inventoryMenu.broadcastChanges();
            NotificationHelper.sendInfo(serverPlayer, "shulkersorter.message.undo_success");
        });
    }

    private static void undoClientAndSyncCreative(Minecraft client) {
        List<ItemStack> snapshot = SortUndoManager.get().getSnapshot();
        SortUndoManager.get().clear();

        if (snapshot == null) {
            NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_nothing");
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
        NotificationHelper.sendInfo(client.player, "shulkersorter.message.undo_success");
    }
}
