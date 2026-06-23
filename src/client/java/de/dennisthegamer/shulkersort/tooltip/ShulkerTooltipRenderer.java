package de.dennisthegamer.shulkersort.tooltip;

import de.dennisthegamer.shulkersort.config.ShulkerSortConfig;
import de.dennisthegamer.shulkersort.util.ShulkerBoxHelper;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShulkerTooltipRenderer {

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            ShulkerSortConfig config = ShulkerSortConfig.getInstance();
            if (!config.tooltipEnabled) return;
            if (!ShulkerBoxHelper.isShulkerBox(stack)) return;

            List<ItemStack> contents = ShulkerBoxHelper.getContents(stack);

            // Aggregate items by type
            Map<String, Integer> itemCounts = new LinkedHashMap<>();
            List<String> itemNames = new ArrayList<>();

            for (ItemStack item : contents) {
                if (item.isEmpty()) continue;
                String name = item.getName().getString();
                itemCounts.merge(name, item.getCount(), Integer::sum);
                if (!itemNames.contains(name)) {
                    itemNames.add(name);
                }
            }

            if (itemCounts.isEmpty()) {
                lines.add(Text.literal("  ").append(Text.translatable("shulkersort.tooltip.empty")).formatted(Formatting.GRAY, Formatting.ITALIC));
                return;
            }

            lines.add(Text.literal("  ").append(Text.translatable("shulkersort.tooltip.contents")).formatted(Formatting.GRAY));

            int shown = 0;
            int maxLines = config.tooltipMaxLines;
            int remaining = 0;

            for (String name : itemNames) {
                if (shown >= maxLines) {
                    remaining += itemCounts.get(name);
                    continue;
                }
                int count = itemCounts.get(name);
                lines.add(Text.literal("  " + name + " x" + count)
                        .formatted(Formatting.GRAY));
                shown++;
            }

            if (remaining > 0) {
                int hiddenTypes = itemNames.size() - maxLines;
                lines.add(Text.literal("  ").append(Text.translatable("shulkersort.tooltip.more", hiddenTypes))
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            }
        });
    }
}
