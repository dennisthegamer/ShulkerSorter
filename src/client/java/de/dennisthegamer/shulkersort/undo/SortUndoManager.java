package de.dennisthegamer.shulkersort.undo;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortUndoManager {
    private static final SortUndoManager INSTANCE = new SortUndoManager();
    private List<ItemStack> snapshot = null;
    private SortUndoManager() {}
    public static SortUndoManager get() { return INSTANCE; }

    public void saveSnapshot(PlayerInventory inventory) {
        snapshot = new ArrayList<>(36);
        for (int i = 0; i < 36; i++) {
            snapshot.add(inventory.getStack(i).copy());
        }
    }

    public boolean hasSnapshot() { return snapshot != null; }
    public List<ItemStack> getSnapshot() { return snapshot == null ? null : Collections.unmodifiableList(snapshot); }
    public void clear() { snapshot = null; }
}
