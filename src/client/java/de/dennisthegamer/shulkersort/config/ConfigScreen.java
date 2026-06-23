package de.dennisthegamer.shulkersort.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.shulkersort.title"))
            .setSavingRunnable(config::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // === SORTING SETTINGS ===
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

        sorting.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.include_loose_items"),
            config.includeLooseItems
        )
            .setDefaultValue(false)
            .setTooltip(Text.translatable("config.shulkersort.include_loose_items.tooltip"))
            .setSaveConsumer(value -> config.includeLooseItems = value)
            .build());

        sorting.addEntry(entryBuilder.startBooleanToggle(
            Text.translatable("config.shulkersort.skip_empty_boxes"),
            config.skipEmptyBoxes
        )
            .setDefaultValue(false)
            .setTooltip(Text.translatable("config.shulkersort.skip_empty_boxes.tooltip"))
            .setSaveConsumer(value -> config.skipEmptyBoxes = value)
            .build());

        sorting.addEntry(entryBuilder.startEnumSelector(
            Text.translatable("config.shulkersort.overflow_mode"),
            OverflowMode.class,
            config.overflowMode
        )
            .setDefaultValue(OverflowMode.FILL)
            .setTooltip(Text.translatable("config.shulkersort.overflow_mode.tooltip"))
            .setSaveConsumer(value -> config.overflowMode = value)
            .build());

        sorting.addEntry(entryBuilder.startStrField(
            Text.translatable("config.shulkersort.loose_item_ignore_tag"),
            config.looseItemIgnoreTag
        )
            .setDefaultValue("[KEEP]")
            .setTooltip(Text.translatable("config.shulkersort.loose_item_ignore_tag.tooltip"))
            .setSaveConsumer(value -> config.looseItemIgnoreTag = value)
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

        // === CATEGORIES ===
        ConfigCategory categoriesTab = builder.getOrCreateCategory(
            Text.translatable("config.shulkersort.category.categories")
        );

        // Category order list
        List<String> defaultCategoryOrder = List.of(
                "redstone", "transport", "nature", "mob_loot", "decoration",
                "blocks", "tools", "food", "ores", "brewing", "misc"
        );
        categoriesTab.addEntry(entryBuilder.startStrList(
            Text.translatable("config.shulkersort.category_order"),
            new ArrayList<>(config.categoryOrder)
        )
            .setDefaultValue(new ArrayList<>(defaultCategoryOrder))
            .setTooltip(Text.translatable("config.shulkersort.category_order.tooltip"))
            .setSaveConsumer(value -> config.categoryOrder = new ArrayList<>(value))
            .build());

        // Per-category: enabled toggle + patterns list
        for (String catKey : config.categoryOrder) {
            CategoryDefinition catDef = config.categories.get(catKey);
            if (catDef == null) continue;

            // Enabled/disabled toggle
            categoriesTab.addEntry(entryBuilder.startBooleanToggle(
                Text.translatable(catDef.getLabelPrefix())
                    .append(Text.literal(" - "))
                    .append(Text.translatable("config.shulkersort.category.enabled")),
                !config.disabledCategories.contains(catKey)
            )
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.shulkersort.category.enabled.tooltip"))
                .setSaveConsumer(value -> {
                    if (value) {
                        config.disabledCategories.remove(catKey);
                    } else {
                        config.disabledCategories.add(catKey);
                    }
                })
                .build());

            // Patterns list
            final CategoryDefinition finalCatDef = catDef;
            categoriesTab.addEntry(entryBuilder.startStrList(
                Text.translatable(catDef.getLabelPrefix())
                    .append(Text.literal(" - "))
                    .append(Text.translatable("config.shulkersort.category.patterns")),
                new ArrayList<>(catDef.getPatterns())
            )
                .setDefaultValue(new ArrayList<>(catDef.getPatterns()))
                .setTooltip(Text.translatable("config.shulkersort.category.patterns.tooltip"))
                .setSaveConsumer(value -> finalCatDef.setPatterns(value))
                .build());
        }

        return builder.build();
    }
}
