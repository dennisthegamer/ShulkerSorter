package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.sort.ShulkerSorterEngine;
import de.dennisthegamer.shulkersorter.sort.SortResult;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ServerSortHandler {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(SortRequestPayload.TYPE, SortRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SortResultPayload.TYPE, SortResultPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SortRequestPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleRequest(player, payload));
        });
    }

    private static void handleRequest(ServerPlayer player, SortRequestPayload payload) {
        switch (payload.action()) {
            case SORT -> handleSort(player);
            case UNDO -> handleUndo(player);
        }
    }

    private static void handleSort(ServerPlayer player) {
        ServerUndoManager.get().saveSnapshot(player);

        SortResult result = ShulkerSorterEngine.sort(player.getInventory());
        player.inventoryMenu.broadcastChanges();

        if (!result.success()) {
            ServerUndoManager.get().clear(player.getUUID());
        }

        ServerPlayNetworking.send(player, new SortResultPayload(
                result.success(), result.boxesSorted(), result.itemsMoved(), result.errorMessage()));
    }

    private static void handleUndo(ServerPlayer player) {
        boolean restored = ServerUndoManager.get().restore(player);
        if (restored) {
            ServerPlayNetworking.send(player, new SortResultPayload(true, 0, 0, "shulkersorter.message.undo_success"));
        } else {
            ServerPlayNetworking.send(player, new SortResultPayload(false, 0, 0, "shulkersorter.message.undo_nothing"));
        }
    }
}
