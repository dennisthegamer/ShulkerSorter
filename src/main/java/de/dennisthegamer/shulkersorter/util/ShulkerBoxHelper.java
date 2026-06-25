package de.dennisthegamer.shulkersorter.util;

import de.dennisthegamer.shulkersorter.config.ShulkerSorterConfig;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class ShulkerBoxHelper {

    public static boolean isShulkerBox(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return false;
        return blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static List<ItemStack> getContents(ItemStack shulkerStack) {
        if (!isShulkerBox(shulkerStack)) return List.of();

        ContainerComponent container = shulkerStack.getOrDefault(
                DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

        DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
        container.copyTo(items);

        return new ArrayList<>(items);
    }

    public static void setContents(ItemStack shulkerStack, List<ItemStack> contents) {
        if (!isShulkerBox(shulkerStack)) return;

        // Pad to 27 slots
        DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(contents.size(), 27); i++) {
            items.set(i, contents.get(i));
        }

        ContainerComponent container = ContainerComponent.fromStacks(items);
        shulkerStack.set(DataComponentTypes.CONTAINER, container);
    }

    public static boolean isLocked(ItemStack shulkerStack) {
        Text name = getCustomName(shulkerStack);
        if (name == null) return false;
        String nameStr = name.getString();
        return nameStr.contains(ShulkerSorterConfig.getInstance().lockedTag);
    }

    public static boolean isEmpty(ItemStack shulkerStack) {
        List<ItemStack> contents = getContents(shulkerStack);
        for (ItemStack item : contents) {
            if (!item.isEmpty()) return false;
        }
        return true;
    }

    public static Text getCustomName(ItemStack shulkerStack) {
        return shulkerStack.get(DataComponentTypes.CUSTOM_NAME);
    }

    public static void setCustomName(ItemStack shulkerStack, Text name) {
        shulkerStack.set(DataComponentTypes.CUSTOM_NAME, name);
    }

    public static List<ShulkerBoxInfo> findShulkerBoxes(PlayerInventory inventory) {
        List<ShulkerBoxInfo> result = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (isShulkerBox(stack)) {
                result.add(new ShulkerBoxInfo(i, stack));
            }
        }
        return result;
    }

    public record ShulkerBoxInfo(int slot, ItemStack stack) {}
}
