package de.dennisthegamer.shulkersort.network;

import de.dennisthegamer.shulkersort.util.NotificationHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ClientSortHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SortResultPayload.ID, (payload, context) -> {
            context.client().execute(() -> handleResult(payload));
        });
    }

    private static void handleResult(SortResultPayload payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (payload.errorKey() != null && payload.errorKey().startsWith("shulkersort.message.undo")) {
            NotificationHelper.sendInfo(client.player, payload.errorKey());
        } else if (payload.success()) {
            NotificationHelper.sendSuccess(client.player, payload.boxesSorted(), payload.itemsMoved());
        } else {
            NotificationHelper.sendError(client.player, payload.errorKey());
        }
    }
}
