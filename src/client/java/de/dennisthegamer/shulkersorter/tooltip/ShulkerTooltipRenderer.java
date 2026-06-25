package de.dennisthegamer.shulkersorter.tooltip;

import de.dennisthegamer.shulkersorter.config.ShulkerSorterConfig;
import de.dennisthegamer.shulkersorter.util.ShulkerBoxHelper;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShulkerTooltipRenderer {

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            ShulkerSorterConfig config = ShulkerSorterConfig.getInstance();
            if (!config.tooltipEnabled) return;
            if (!ShulkerBoxHelper.isShulkerBox(stack)) return;

            List<ItemStack> contents = ShulkerBoxHelper.getContents(stack);

            // Aggregate items by type
            Map<String, Integer> itemCounts = new LinkedHashMap<>();
            List<String> itemNames = new ArrayList<>();

            for (ItemStack item : contents) {
                if (item.isEmpty()) continue;
                String name = item.getHoverName().getString();
                itemCounts.merge(name, item.getCount(), Integer::sum);
                if (!itemNames.contains(name)) {
                    itemNames.add(name);
                }
            }

            if (itemCounts.isEmpty()) {
                lines.add(Component.literal("  ").append(Component.translatable("shulkersorter.tooltip.empty")).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                return;
            }

            lines.add(Component.literal("  ").append(Component.translatable("shulkersorter.tooltip.contents")).withStyle(ChatFormatting.GRAY));

            int shown = 0;
            int maxLines = config.tooltipMaxLines;
            int remaining = 0;

            for (String name : itemNames) {
                if (shown >= maxLines) {
                    remaining += itemCounts.get(name);
                    continue;
                }
                int count = itemCounts.get(name);
                lines.add(Component.literal("  " + name + " x" + count)
                        .withStyle(ChatFormatting.GRAY));
                shown++;
            }

            if (remaining > 0) {
                lines.add(Component.literal("  ").append(Component.translatable("shulkersorter.tooltip.more", remaining))
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        });
    }
}
