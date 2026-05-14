package com.shulkersort.util;

import com.shulkersort.config.ShulkerSortConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class NotificationHelper {

    public static void sendSuccess(Player player, int boxCount, int itemCount) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();

        if (config.enableChatNotifications) {
            Component message = Component.translatable("shulkersort.message.success", boxCount, itemCount)
                    .withStyle(ChatFormatting.GREEN);
            player.sendSystemMessage(message);
        }

        if (config.enableSoundEffects) {
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5f,
                    1.0f
            );
        }
    }

    public static void sendInfo(Player player, String messageKey) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();
        if (config.enableChatNotifications) {
            Component text = Component.translatable(messageKey)
                    .withStyle(ChatFormatting.YELLOW);
            player.sendSystemMessage(text);
        }
    }

    public static void sendError(Player player, String message) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();

        if (config.enableChatNotifications) {
            Component text = Component.translatable(message)
                    .withStyle(ChatFormatting.RED);
            player.sendSystemMessage(text);
        }
    }
}
