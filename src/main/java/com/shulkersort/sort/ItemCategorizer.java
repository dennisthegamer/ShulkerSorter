package com.shulkersort.sort;

import com.shulkersort.config.CategoryDefinition;
import com.shulkersort.config.ShulkerSortConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemCategorizer {

    public static String categorize(ItemStack stack) {
        if (stack.isEmpty()) return "misc";

        ShulkerSortConfig config = ShulkerSortConfig.getInstance();
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemId = id.getPath(); // e.g. "oak_planks"

        // Check food component first (special handling)
        boolean isFood = stack.has(DataComponents.FOOD);

        // Pass 1: exact, prefix, and suffix matches (high specificity)
        for (String categoryKey : config.categoryOrder) {
            if (config.disabledCategories.contains(categoryKey)) continue;
            CategoryDefinition category = config.getCategory(categoryKey);
            if (category == null) continue;

            if (categoryKey.equals("food") && isFood) {
                return "food";
            }

            for (String pattern : category.getPatterns()) {
                if (matchesSpecific(itemId, pattern)) {
                    return categoryKey;
                }
            }
        }

        // Pass 2: contains matches (lower specificity, checked after all specific matches)
        for (String categoryKey : config.categoryOrder) {
            if (config.disabledCategories.contains(categoryKey)) continue;
            CategoryDefinition category = config.getCategory(categoryKey);
            if (category == null) continue;

            for (String pattern : category.getPatterns()) {
                if (!isSpecificPattern(pattern) && itemId.contains(pattern)) {
                    return categoryKey;
                }
            }
        }

        return "misc";
    }

    private static boolean isSpecificPattern(String pattern) {
        return pattern.startsWith("_") || pattern.endsWith("_");
    }

    private static boolean matchesSpecific(String itemId, String pattern) {
        // Prefix pattern: "raw_" matches "raw_iron", "raw_gold"
        if (pattern.endsWith("_")) {
            return itemId.startsWith(pattern);
        }
        // Suffix pattern: "_planks" matches "oak_planks", "birch_planks"
        if (pattern.startsWith("_")) {
            return itemId.endsWith(pattern) || itemId.contains(pattern + "_");
        }
        // Exact match only (contains is handled in pass 2)
        return itemId.equals(pattern);
    }
}
