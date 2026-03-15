package com.shulkersort.sort;

import com.shulkersort.config.CategoryDefinition;
import com.shulkersort.config.ShulkerSortConfig;
import com.shulkersort.util.ShulkerBoxHelper;
import com.shulkersort.util.ShulkerBoxHelper.ShulkerBoxInfo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

public class ShulkerSortEngine {

    public static SortResult sort(PlayerInventory inventory) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();

        // Phase 1: SCAN - Find all shulker boxes, filter locked/empty
        List<ShulkerBoxInfo> allBoxes = ShulkerBoxHelper.findShulkerBoxes(inventory);
        List<ShulkerBoxInfo> sortableBoxes = new ArrayList<>();
        int lockedCount = 0;

        for (ShulkerBoxInfo box : allBoxes) {
            if (ShulkerBoxHelper.isLocked(box.stack())) {
                lockedCount++;
                continue;
            }
            sortableBoxes.add(box);
        }

        if (sortableBoxes.isEmpty()) {
            if (lockedCount > 0) {
                return SortResult.error("shulkersort.message.error.all_locked");
            }
            return SortResult.error("shulkersort.message.error.no_boxes");
        }

        // Phase 2: CATEGORIZE - Extract and categorize all items
        Map<String, List<ItemStack>> categorizedItems = new LinkedHashMap<>();
        for (String cat : config.categoryOrder) {
            categorizedItems.put(cat, new ArrayList<>());
        }

        int totalItems = 0;
        for (ShulkerBoxInfo box : sortableBoxes) {
            List<ItemStack> contents = ShulkerBoxHelper.getContents(box.stack());
            for (ItemStack item : contents) {
                if (item.isEmpty()) continue;
                String category = ItemCategorizer.categorize(item);
                categorizedItems.computeIfAbsent(category, k -> new ArrayList<>()).add(item.copy());
                totalItems += item.getCount();
            }
        }

        if (totalItems == 0) {
            return SortResult.error("shulkersort.message.error.all_empty");
        }

        // Phase 3: MERGE - Merge stacks of same items
        for (Map.Entry<String, List<ItemStack>> entry : categorizedItems.entrySet()) {
            entry.setValue(mergeStacks(entry.getValue()));
        }

        // Phase 4: VALIDATE - Check capacity
        int totalSlotsNeeded = 0;
        for (List<ItemStack> items : categorizedItems.values()) {
            totalSlotsNeeded += items.size();
        }
        int totalSlotsAvailable = sortableBoxes.size() * 27;

        if (totalSlotsNeeded > totalSlotsAvailable) {
            return SortResult.error("shulkersort.message.error.not_enough_space");
        }

        // Phase 5: DISTRIBUTE - Fill boxes sequentially per category
        List<List<ItemStack>> newBoxContents = new ArrayList<>();
        for (int i = 0; i < sortableBoxes.size(); i++) {
            newBoxContents.add(new ArrayList<>(Collections.nCopies(27, ItemStack.EMPTY)));
        }

        List<String> boxCategories = new ArrayList<>(); // Track which category each box belongs to
        int currentBoxIndex = 0;
        int currentSlotIndex = 0;

        for (String categoryKey : config.categoryOrder) {
            List<ItemStack> items = categorizedItems.get(categoryKey);
            if (items == null || items.isEmpty()) continue;

            // If we're in the middle of a box, start a new one for new category
            if (currentSlotIndex > 0) {
                currentBoxIndex++;
                currentSlotIndex = 0;
            }

            for (ItemStack item : items) {
                if (currentBoxIndex >= sortableBoxes.size()) {
                    // Should not happen due to validation, but safety check
                    return SortResult.error("shulkersort.message.error.internal");
                }

                newBoxContents.get(currentBoxIndex).set(currentSlotIndex, item);

                // Track category for this box
                while (boxCategories.size() <= currentBoxIndex) {
                    boxCategories.add(null);
                }
                boxCategories.set(currentBoxIndex, categoryKey);

                currentSlotIndex++;
                if (currentSlotIndex >= 27) {
                    currentBoxIndex++;
                    currentSlotIndex = 0;
                }
            }
        }

        // Phase 6: LABEL - Auto-label boxes
        Map<String, Integer> categoryCounters = new LinkedHashMap<>();
        List<Text> newNames = new ArrayList<>();

        for (int i = 0; i < sortableBoxes.size(); i++) {
            String category = (i < boxCategories.size()) ? boxCategories.get(i) : null;

            if (category != null && config.autoLabel) {
                CategoryDefinition catDef = config.getCategory(category);
                String prefix = catDef != null ? catDef.getLabelPrefix() : category;
                int count = categoryCounters.merge(category, 1, Integer::sum);
                newNames.add(Text.translatable(prefix).append(Text.literal(" #" + count)));
            } else {
                newNames.add(null); // Don't rename empty boxes
            }
        }

        // Phase 7: APPLY - Atomic commit
        int boxesSorted = 0;
        for (int i = 0; i < sortableBoxes.size(); i++) {
            ShulkerBoxInfo box = sortableBoxes.get(i);
            ItemStack shulkerStack = box.stack();

            ShulkerBoxHelper.setContents(shulkerStack, newBoxContents.get(i));

            if (newNames.get(i) != null) {
                ShulkerBoxHelper.setCustomName(shulkerStack, newNames.get(i));
                boxesSorted++;
            }
        }

        return SortResult.success(boxesSorted, totalItems);
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> items) {
        if (items.isEmpty()) return items;

        List<ItemStack> merged = new ArrayList<>();

        for (ItemStack item : items) {
            if (item.isEmpty()) continue;

            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.areItemsAndComponentsEqual(existing, item)) {
                    int maxSize = existing.getMaxCount();
                    int canAdd = maxSize - existing.getCount();
                    if (canAdd > 0) {
                        int toAdd = Math.min(canAdd, item.getCount());
                        existing.increment(toAdd);
                        item.decrement(toAdd);
                        if (item.isEmpty()) {
                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found && !item.isEmpty()) {
                merged.add(item.copy());
            }
        }

        return merged;
    }
}
