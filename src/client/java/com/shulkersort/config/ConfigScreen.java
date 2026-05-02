package com.shulkersort.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen {

    public static Screen create(Screen parent) {
        ShulkerSortConfig config = ShulkerSortConfig.getInstance();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.shulkersort.title"))

                // === Sorting Settings ===
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.shulkersort.category.sorting"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.shulkersort.auto_label"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.auto_label.tooltip")))
                                .binding(true, () -> config.autoLabel, val -> config.autoLabel = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.shulkersort.include_loose_items"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.include_loose_items.tooltip")))
                                .binding(false, () -> config.includeLooseItems, val -> config.includeLooseItems = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("config.shulkersort.loose_item_ignore_tag"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.loose_item_ignore_tag.tooltip")))
                                .binding("[KEEP]", () -> config.looseItemIgnoreTag, val -> config.looseItemIgnoreTag = val)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Component.translatable("config.shulkersort.locked_tag"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.locked_tag.tooltip")))
                                .binding("[LOCKED]", () -> config.lockedTag, val -> config.lockedTag = val)
                                .controller(StringControllerBuilder::create)
                                .build())
                        .build())

                // === Tooltip Settings ===
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.shulkersort.category.tooltip"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.shulkersort.tooltip_enabled"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.tooltip_enabled.tooltip")))
                                .binding(true, () -> config.tooltipEnabled, val -> config.tooltipEnabled = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("config.shulkersort.tooltip_max_lines"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.tooltip_max_lines.tooltip")))
                                .binding(5, () -> config.tooltipMaxLines, val -> config.tooltipMaxLines = val)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 27).step(1))
                                .build())
                        .build())

                // === Feedback Settings ===
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.shulkersort.category.feedback"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.shulkersort.enable_chat_notifications"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.enable_chat_notifications.tooltip")))
                                .binding(true, () -> config.enableChatNotifications, val -> config.enableChatNotifications = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.shulkersort.enable_sound_effects"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.enable_sound_effects.tooltip")))
                                .binding(true, () -> config.enableSoundEffects, val -> config.enableSoundEffects = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("config.shulkersort.enable_hud_overlay"))
                                .description(OptionDescription.of(Component.translatable("config.shulkersort.enable_hud_overlay.tooltip")))
                                .binding(true, () -> config.enableHudOverlay, val -> config.enableHudOverlay = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())

                .save(config::save)
                .build()
                .generateScreen(parent);
    }
}
