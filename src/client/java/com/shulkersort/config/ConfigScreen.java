package com.shulkersort.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.shulkersort.title"))
            .setSavingRunnable(config::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // === SORTING FEATURES ===
        ConfigCategory sorting = builder.getOrCreateCategory(
            Text.translatable("config.shulkersort.category.sorting")
        );

        sorting.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.auto_label"),
            config.autoLabel
        )
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.shulkersort.auto_label.tooltip"))
            .setSaveConsumer(value -> config.autoLabel = value)
            .build());

        sorting.addEntry(entryBuilder.startStrField(
            Text.translatable("config.shulkersort.locked_tag"),
            config.lockedTag
        )
            .setDefaultValue("[LOCKED]")
            .setTooltip(Text.translatable("config.shulkersort.locked_tag.tooltip"))
            .setSaveConsumer(value -> config.lockedTag = value)
            .build());

        // === TOOLTIP SETTINGS ===
        ConfigCategory tooltip = builder.getOrCreateCategory(
            Text.translatable("config.shulkersort.category.tooltip")
        );

        tooltip.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.tooltip_enabled"),
            config.tooltipEnabled
        )
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.shulkersort.tooltip_enabled.tooltip"))
            .setSaveConsumer(value -> config.tooltipEnabled = value)
            .build());

        tooltip.addEntry(entryBuilder.startIntField(
            Text.translatable("config.shulkersort.tooltip_max_lines"),
            config.tooltipMaxLines
        )
            .setDefaultValue(5)
            .setMin(1)
            .setMax(27)
            .setTooltip(Text.translatable("config.shulkersort.tooltip_max_lines.tooltip"))
            .setSaveConsumer(value -> config.tooltipMaxLines = value)
            .build());

        // === FEEDBACK SETTINGS ===
        ConfigCategory feedback = builder.getOrCreateCategory(
            Text.translatable("config.shulkersort.category.feedback")
        );

        feedback.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.enable_chat_notifications"),
            config.enableChatNotifications
        )
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.shulkersort.enable_chat_notifications.tooltip"))
            .setSaveConsumer(value -> config.enableChatNotifications = value)
            .build());

        feedback.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.enable_sound_effects"),
            config.enableSoundEffects
        )
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.shulkersort.enable_sound_effects.tooltip"))
            .setSaveConsumer(value -> config.enableSoundEffects = value)
            .build());

        feedback.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.enable_hud_overlay"),
            config.enableHudOverlay
        )
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.shulkersort.enable_hud_overlay.tooltip"))
            .setSaveConsumer(value -> config.enableHudOverlay = value)
            .build());

        // === ADVANCED OPTIONS ===
        ConfigCategory advanced = builder.getOrCreateCategory(
            Text.translatable("config.shulkersort.category.advanced")
        );

        advanced.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.enable_debug_logging"),
            config.enableDebugLogging
        )
            .setDefaultValue(false)
            .setTooltip(Text.translatable("config.shulkersort.enable_debug_logging.tooltip"))
            .setSaveConsumer(value -> config.enableDebugLogging = value)
            .build());

        return builder.build();
    }
}
