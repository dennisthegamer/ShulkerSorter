package com.shulkersort.sort;

import com.shulkersort.config.CategoryDefinition;
import com.shulkersort.config.ShulkerSortConfig;
import com.shulkersort.util.ShulkerBoxHelper;
import com.shulkersort.util.ShulkerBoxHelper.ShulkerBoxInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ShulkerSortEngine {

    public static SortResult sort(Inventory inventory) {
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

        // Track which category each box primarily contained before sorting
        Map<Integer, String> boxPrimaryCategory = new HashMap<>();
        int totalItems = 0;
        for (int boxIdx = 0; boxIdx < sortableBoxes.size(); boxIdx++) {
            ShulkerBoxInfo box = sortableBoxes.get(boxIdx);
            List<ItemStack> contents = ShulkerBoxHelper.getContents(box.stack());
            Map<String, Integer> catCounts = new HashMap<>();
            for (ItemStack item : contents) {
                if (item.isEmpty()) continue;
                String category = ItemCategorizer.categorize(item);
                categorizedItems.computeIfAbsent(category, k -> new ArrayList<>()).add(item.copy());
                totalItems += item.getCount();
                catCounts.merge(category, item.getCount(), Integer::sum);
            }
            // Determine the dominant category for this box
            if (!catCounts.isEmpty()) {
                boxPrimaryCategory.put(boxIdx, catCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue()).get().getKey());
            }
        }

        // Collect loose (non-shulker) items from inventory if enabled
        List<Integer> looseItemSlots = new ArrayList<>();
        if (config.includeLooseItems) {
            String ignoreTag = config.looseItemIgnoreTag;
            for (int i = 0; i < 36; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty() || ShulkerBoxHelper.isShulkerBox(stack)) continue;
                // Skip items with the ignore tag in their custom name
                if (!ignoreTag.isEmpty() && stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                    String name = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME).getString();
                    if (name.contains(ignoreTag)) continue;
                }
                String category = ItemCategorizer.categorize(stack);
                categorizedItems.computeIfAbsent(category, k -> new ArrayList<>()).add(stack.copy());
                totalItems += stack.getCount();
                looseItemSlots.add(i);
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

        // Phase 5: DISTRIBUTE - Fill boxes by category, preserving box-category affinity
        List<List<ItemStack>> newBoxContents = new ArrayList<>();
        for (int i = 0; i < sortableBoxes.size(); i++) {
            newBoxContents.add(new ArrayList<>(Collections.nCopies(27, ItemStack.EMPTY)));
        }

        String[] boxCategories = new String[sortableBoxes.size()];

        // Build ordered list of boxes for each category:
        // 1. Boxes that previously contained this category (affinity match)
        // 2. Remaining unassigned boxes in inventory order
        Set<Integer> assignedBoxes = new HashSet<>();

        for (String categoryKey : config.categoryOrder) {
            List<ItemStack> items = categorizedItems.get(categoryKey);
            if (items == null || items.isEmpty()) continue;

            // Collect boxes with affinity for this category (not yet assigned)
            List<Integer> affinityBoxes = new ArrayList<>();
            for (int i = 0; i < sortableBoxes.size(); i++) {
                if (!assignedBoxes.contains(i) && categoryKey.equals(boxPrimaryCategory.get(i))) {
                    affinityBoxes.add(i);
                }
            }

            // Calculate how many boxes this category needs
            int boxesNeeded = (items.size() + 26) / 27;

            // Start filling: first use affinity boxes, then any unassigned box
            int itemIdx = 0;
            int boxesUsed = 0;

            // Fill affinity boxes first
            for (int boxIdx : affinityBoxes) {
                if (itemIdx >= items.size()) break;

                assignedBoxes.add(boxIdx);
                boxCategories[boxIdx] = categoryKey;
                int slotIdx = 0;
                while (slotIdx < 27 && itemIdx < items.size()) {
                    newBoxContents.get(boxIdx).set(slotIdx, items.get(itemIdx));
                    slotIdx++;
                    itemIdx++;
                }
                boxesUsed++;
            }

            // If we still have items, use next unassigned boxes
            if (itemIdx < items.size()) {
                for (int boxIdx = 0; boxIdx < sortableBoxes.size() && itemIdx < items.size(); boxIdx++) {
                    if (assignedBoxes.contains(boxIdx)) continue;

                    assignedBoxes.add(boxIdx);
                    boxCategories[boxIdx] = categoryKey;
                    int slotIdx = 0;
                    while (slotIdx < 27 && itemIdx < items.size()) {
                        newBoxContents.get(boxIdx).set(slotIdx, items.get(itemIdx));
                        slotIdx++;
                        itemIdx++;
                    }
                }
            }

            if (itemIdx < items.size()) {
                return SortResult.error("shulkersort.message.error.internal");
            }
        }

        // Phase 6: LABEL - Auto-label boxes
        Map<String, Integer> categoryCounters = new LinkedHashMap<>();
        List<Component> newNames = new ArrayList<>();

        for (int i = 0; i < sortableBoxes.size(); i++) {
            String category = boxCategories[i];

            if (category != null && config.autoLabel) {
                CategoryDefinition catDef = config.getCategory(category);
                String prefix = catDef != null ? catDef.getLabelPrefix() : category;
                int count = categoryCounters.merge(category, 1, Integer::sum);
                newNames.add(Component.translatable(prefix).append(Component.literal(" #" + count)));
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

        // Clear loose item slots that were absorbed into shulker boxes
        for (int slot : looseItemSlots) {
            inventory.setItem(slot, ItemStack.EMPTY);
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
                if (ItemStack.isSameItemSameComponents(existing, item)) {
                    int maxSize = existing.getMaxStackSize();
                    int canAdd = maxSize - existing.getCount();
                    if (canAdd > 0) {
                        int toAdd = Math.min(canAdd, item.getCount());
                        existing.grow(toAdd);
                        item.shrink(toAdd);
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
