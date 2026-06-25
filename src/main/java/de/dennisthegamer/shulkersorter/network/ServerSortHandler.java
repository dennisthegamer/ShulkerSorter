package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.sort.ShulkerSorterEngine;
import de.dennisthegamer.shulkersorter.sort.SortResult;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerSortHandler {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(SortRequestPayload.ID, SortRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SortResultPayload.ID, SortResultPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SortRequestPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> handleRequest(player, payload));
        });
    }

    private static void handleRequest(ServerPlayerEntity player, SortRequestPayload payload) {
        switch (payload.action()) {
            case SORT -> handleSort(player);
            case UNDO -> handleUndo(player);
        }
    }

    private static void handleSort(ServerPlayerEntity player) {
        ServerUndoManager.get().saveSnapshot(player);

        SortResult result = ShulkerSorterEngine.sort(player.getInventory());
        player.playerScreenHandler.sendContentUpdates();

        if (!result.success()) {
            ServerUndoManager.get().clear(player.getUuid());
        }

        ServerPlayNetworking.send(player, new SortResultPayload(
                result.success(), result.boxesSorted(), result.itemsMoved(), result.errorMessage()));
    }

    private static void handleUndo(ServerPlayerEntity player) {
        boolean restored = ServerUndoManager.get().restore(player);
        if (restored) {
            ServerPlayNetworking.send(player, new SortResultPayload(true, 0, 0, "shulkersorter.message.undo_success"));
        } else {
            ServerPlayNetworking.send(player, new SortResultPayload(false, 0, 0, "shulkersorter.message.undo_nothing"));
        }
    }
}
