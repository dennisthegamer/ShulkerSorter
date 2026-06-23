package de.dennisthegamer.shulkersort.sort;

import de.dennisthegamer.shulkersort.config.CategoryDefinition;
import de.dennisthegamer.shulkersort.config.ShulkerSortConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public class ItemCategorizer {

    public static String categorize(ItemStack stack) {
        if (stack.isEmpty()) return "misc";

        ShulkerSortConfig config = ShulkerSortConfig.getInstance();
        Identifier id = Registries.ITEM.getId(stack.getItem());
        String itemId = id.getPath(); // e.g. "oak_planks"

        // Check food component first (special handling)
        boolean isFood = stack.contains(DataComponentTypes.FOOD);

        for (String categoryKey : config.categoryOrder) {
            CategoryDefinition category = config.getCategory(categoryKey);
            if (category == null) continue;

            // Special: food category matches food component
            if (categoryKey.equals("food") && isFood) {
                return "food";
            }

            // Pattern matching against registry ID
            for (String pattern : category.getPatterns()) {
                if (matchesPattern(itemId, pattern)) {
                    return categoryKey;
                }
            }
        }

        return "misc";
    }

    private static boolean matchesPattern(String itemId, String pattern) {
        // Prefix pattern: "raw_" matches "raw_iron", "raw_gold"
        if (pattern.endsWith("_")) {
            return itemId.startsWith(pattern);
        }
        // Suffix pattern: "_planks" matches "oak_planks", "birch_planks"
        if (pattern.startsWith("_")) {
            return itemId.endsWith(pattern) || itemId.contains(pattern + "_");
        }
        // Exact or contains match
        return itemId.equals(pattern) || itemId.contains(pattern);
    }
}
