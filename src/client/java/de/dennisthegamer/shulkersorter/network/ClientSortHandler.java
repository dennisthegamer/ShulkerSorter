package de.dennisthegamer.shulkersorter.network;

import de.dennisthegamer.shulkersorter.util.NotificationHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class ClientSortHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SortResultPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> handleResult(payload));
        });
    }

    private static void handleResult(SortResultPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (payload.errorKey() != null && payload.errorKey().startsWith("shulkersorter.message.undo")) {
            NotificationHelper.sendInfo(client.player, payload.errorKey());
        } else if (payload.success()) {
            NotificationHelper.sendSuccess(client.player, payload.boxesSorted(), payload.itemsMoved());
        } else {
            NotificationHelper.sendError(client.player, payload.errorKey());
        }
    }
}
