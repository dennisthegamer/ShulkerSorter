package de.dennisthegamer.shulkersorter.network;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class ServerUndoManager {
    private static final ServerUndoManager INSTANCE = new ServerUndoManager();
    private final Map<UUID, List<ItemStack>> snapshots = new HashMap<>();

    private ServerUndoManager() {}

    public static ServerUndoManager get() { return INSTANCE; }

    public void saveSnapshot(ServerPlayerEntity player) {
        List<ItemStack> snapshot = new ArrayList<>(36);
        for (int i = 0; i < 36; i++) {
            snapshot.add(player.getInventory().getStack(i).copy());
        }
        snapshots.put(player.getUuid(), snapshot);
    }

    public boolean restore(ServerPlayerEntity player) {
        List<ItemStack> snapshot = snapshots.remove(player.getUuid());
        if (snapshot == null) return false;
        for (int i = 0; i < 36; i++) {
            player.getInventory().setStack(i, snapshot.get(i).copy());
        }
        player.playerScreenHandler.sendContentUpdates();
        return true;
    }

    public boolean hasSnapshot(UUID uuid) {
        return snapshots.containsKey(uuid);
    }

    public void clear(UUID uuid) {
        snapshots.remove(uuid);
    }
}
