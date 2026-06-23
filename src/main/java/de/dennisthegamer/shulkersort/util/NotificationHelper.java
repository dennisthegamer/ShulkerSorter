package de.dennisthegamer.shulkersort.util;

import de.dennisthegamer.shulkersort.config.ShulkerSortConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NotificationHelper {

    public static void sendSuccess(PlayerEntity player, int boxCount, int itemCount) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();
        if (config.enableChatNotifications) {
            Text message = Text.translatable("shulkersort.message.success", boxCount, itemCount)
                    .formatted(Formatting.GREEN);
            player.sendMessage(message, false);
        }
        if (config.enableSoundEffects) {
            player.getWorld().playSound(
                    null, player.getBlockPos(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS, 0.5f, 1.0f);
        }
    }

    public static void sendInfo(PlayerEntity player, String messageKey) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();
        if (config.enableChatNotifications) {
            Text text = Text.translatable(messageKey).formatted(Formatting.YELLOW);
            player.sendMessage(text, false);
        }
    }

    public static void sendError(PlayerEntity player, String message) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();
        if (config.enableChatNotifications) {
            Text text = Text.translatable(message).formatted(Formatting.RED);
            player.sendMessage(text, false);
        }
    }
}
