package de.dennisthegamer.shulkersorter.config;

import de.dennisthegamer.shulkersorter.ShulkerSorter;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ShulkerSorterConfig {
    private static ShulkerSorterConfig INSTANCE;

    private final Path configPath;

    // Sorting Settings
    public boolean autoLabel = true;
    public String lockedTag = "[LOCKED]";

    // Tooltip Settings
    public boolean tooltipEnabled = true;
    public int tooltipMaxLines = 5;

    // Feedback Settings
    public boolean enableChatNotifications = true;
    public boolean enableSoundEffects = true;
    public boolean enableHudOverlay = true;

    // Sorting Behavior
    public boolean includeLooseItems = false;
    public boolean skipEmptyBoxes = false;
    public String looseItemIgnoreTag = "[KEEP]";
    public OverflowMode overflowMode = OverflowMode.FILL;

    public List<String> categoryOrder = new ArrayList<>(List.of(
            "redstone", "transport", "nature", "mob_loot", "decoration",
            "blocks", "tools", "food", "ores", "brewing", "misc"
    ));
    public Set<String> disabledCategories = new LinkedHashSet<>();
    public Map<String, CategoryDefinition> categories = new LinkedHashMap<>();

    private ShulkerSorterConfig() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("shulkersorter.toml");
        initDefaultCategories();
    }

    public static ShulkerSorterConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShulkerSorterConfig();
            INSTANCE.load();
        }
        return INSTANCE;
    }

    private void initDefaultCategories() {
        categories.put("redstone", new CategoryDefinition("redstone", "shulkersorter.category.redstone", List.of(
                "repeater", "comparator", "observer", "dispenser", "dropper", "hopper",
                "piston", "lever", "daylight_detector", "target", "note_block", "tnt",
                "tripwire", "trapped_chest", "redstone_torch", "redstone_lamp",
                "redstone_block", "redstone"
        )));
        categories.put("transport", new CategoryDefinition("transport", "shulkersorter.category.transport", List.of(
                "_boat", "_minecart", "_rail", "_horse_armor",
                "minecart", "rail", "saddle", "carrot_on_a_stick", "warped_fungus_on_a_stick"
        )));
        categories.put("nature", new CategoryDefinition("nature", "shulkersorter.category.nature", List.of(
                "_sapling", "_leaves", "_petals",
                "lily_pad", "vine", "bamboo", "sugar_cane", "spore_blossom", "azalea",
                "dripleaf", "hanging_roots", "rooted_dirt", "chorus",
                "nether_sprouts", "warped_roots", "crimson_roots",
                "weeping_vines", "twisting_vines", "dead_bush",
                "grass", "fern", "dandelion", "poppy", "orchid", "allium",
                "bluet", "daisy", "cornflower", "sunflower", "lilac", "peony",
                "tulip", "rose", "torchflower", "fungus"
        )));
        categories.put("mob_loot", new CategoryDefinition("mob_loot", "shulkersorter.category.mob_loot", List.of(
                "bone", "feather", "leather", "string", "slimeball", "rabbit_hide",
                "ink_sac", "glow_ink_sac", "turtle_scute", "armadillo_scute",
                "rotten_flesh", "ender_pearl", "ender_eye", "shulker_shell",
                "prismarine_shard", "prismarine_crystals", "nautilus_shell",
                "heart_of_the_sea", "echo_shard", "nether_star", "totem_of_undying",
                "honeycomb", "honey_bottle", "egg"
        )));
        categories.put("decoration", new CategoryDefinition("decoration", "shulkersorter.category.decoration", List.of(
                "_banner", "_bed", "_candle", "_head", "_skull",
                "torch", "lantern", "painting", "item_frame", "armor_stand",
                "flower_pot", "sea_pickle", "end_rod", "chain", "bell", "music_disc"
        )));
        categories.put("blocks", new CategoryDefinition("blocks", "shulkersorter.category.blocks", List.of(
                "_log", "_planks", "_wood", "_stone", "_brick", "_slab", "_stairs",
                "_wall", "_fence", "_sign", "_hanging_sign",
                "sand", "gravel", "dirt", "glass", "_terracotta", "_concrete", "_wool",
                "_carpet", "_door", "_trapdoor", "_pressure_plate", "_button",
                "cobblestone", "deepslate", "granite", "diorite", "andesite", "tuff",
                "calcite", "mud", "clay", "moss_block", "moss_carpet"
        )));
        categories.put("tools", new CategoryDefinition("tools", "shulkersorter.category.tools", List.of(
                "_sword", "_pickaxe", "_axe", "_shovel", "_hoe",
                "_helmet", "_chestplate", "_leggings", "_boots",
                "bow", "crossbow", "shield", "trident", "fishing_rod",
                "shears", "flint_and_steel", "spyglass", "brush",
                "elytra", "lead", "name_tag", "compass", "clock", "map",
                "mace", "wind_charge"
        )));
        categories.put("food", new CategoryDefinition("food", "shulkersorter.category.food", List.of(
                "_seeds", "apple", "bread", "carrot", "potato",
                "beetroot", "melon_slice", "sweet_berries", "glow_berries",
                "cookie", "pumpkin_pie", "cake", "mushroom_stew",
                "rabbit_stew", "suspicious_stew", "dried_kelp",
                "wheat", "cocoa_beans"
        )));
        categories.put("ores", new CategoryDefinition("ores", "shulkersorter.category.ores", List.of(
                "_ore", "_ingot", "_nugget", "raw_", "_gem",
                "coal", "diamond", "emerald", "lapis_lazuli",
                "quartz", "ancient_debris", "netherite_scrap", "netherite_ingot",
                "amethyst_shard", "copper_ingot", "raw_copper", "raw_iron", "raw_gold"
        )));
        categories.put("brewing", new CategoryDefinition("brewing", "shulkersorter.category.brewing", List.of(
                "potion", "splash_potion", "lingering_potion", "brewing_stand",
                "_dye", "glass_bottle", "blaze_powder", "blaze_rod",
                "nether_wart", "ghast_tear", "magma_cream", "fermented_spider_eye",
                "glistering_melon_slice", "golden_carrot", "rabbit_foot",
                "dragon_breath", "phantom_membrane", "spider_eye",
                "sugar", "gunpowder"
        )));
        categories.put("misc", new CategoryDefinition("misc", "shulkersorter.category.misc", List.of()));
    }

    public void load() {
        if (!configPath.toFile().exists()) {
            save();
            return;
        }

        try {
            Map<String, Object> data = TomlParser.parse(configPath);

            autoLabel = TomlParser.getBoolean(data, "auto_label", autoLabel);
            lockedTag = TomlParser.getString(data, "locked_tag", lockedTag);
            tooltipEnabled = TomlParser.getBoolean(data, "tooltip_enabled", tooltipEnabled);
            tooltipMaxLines = TomlParser.getInt(data, "tooltip_max_lines", tooltipMaxLines);
            enableChatNotifications = TomlParser.getBoolean(data, "enable_chat_notifications", enableChatNotifications);
            enableSoundEffects = TomlParser.getBoolean(data, "enable_sound_effects", enableSoundEffects);
            enableHudOverlay = TomlParser.getBoolean(data, "enable_hud_overlay", enableHudOverlay);
            includeLooseItems = TomlParser.getBoolean(data, "include_loose_items", includeLooseItems);
            skipEmptyBoxes = TomlParser.getBoolean(data, "skip_empty_boxes", skipEmptyBoxes);
            looseItemIgnoreTag = TomlParser.getString(data, "loose_item_ignore_tag", looseItemIgnoreTag);
            categoryOrder = TomlParser.getStringList(data, "category_order", categoryOrder);
            List<String> disabledList = TomlParser.getStringList(data, "disabled_categories", new ArrayList<>());
            disabledCategories = new LinkedHashSet<>(disabledList);
            try {
                overflowMode = OverflowMode.valueOf(
                    TomlParser.getString(data, "overflow_mode", overflowMode.name()).toUpperCase());
            } catch (IllegalArgumentException ignored) {}


            // Load custom categories if present
            Map<String, Object> categoriesTable = TomlParser.getTable(data, "categories");
            if (categoriesTable != null) {
                for (Map.Entry<String, Object> entry : categoriesTable.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> catData = (Map<String, Object>) entry.getValue();
                        List<String> patterns = TomlParser.getStringList(catData, "patterns", List.of());
                        // For built-in categories, always use the translation key from code
                        CategoryDefinition existing = categories.get(entry.getKey());
                        String label = existing != null ? existing.getLabelPrefix()
                                : TomlParser.getString(catData, "label", entry.getKey());
                        categories.put(entry.getKey(), new CategoryDefinition(entry.getKey(), label, patterns));
                    }
                }
            }
        } catch (IOException e) {
            ShulkerSorter.LOGGER.error("Failed to load config, using defaults", e);
        }
    }

    public void save() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("auto_label", autoLabel);
            data.put("locked_tag", lockedTag);
            data.put("tooltip_enabled", tooltipEnabled);
            data.put("tooltip_max_lines", tooltipMaxLines);
            data.put("enable_chat_notifications", enableChatNotifications);
            data.put("enable_sound_effects", enableSoundEffects);
            data.put("enable_hud_overlay", enableHudOverlay);
            data.put("include_loose_items", includeLooseItems);
            data.put("skip_empty_boxes", skipEmptyBoxes);
            data.put("loose_item_ignore_tag", looseItemIgnoreTag);
            data.put("overflow_mode", overflowMode.name());
            data.put("category_order", categoryOrder);
            data.put("disabled_categories", new ArrayList<>(disabledCategories));

            Map<String, Object> categoriesMap = new LinkedHashMap<>();
            for (Map.Entry<String, CategoryDefinition> entry : categories.entrySet()) {
                Map<String, Object> catData = new LinkedHashMap<>();
                catData.put("label", entry.getValue().getLabelPrefix());
                catData.put("patterns", entry.getValue().getPatterns());
                categoriesMap.put(entry.getKey(), catData);
            }
            data.put("categories", categoriesMap);

            configPath.getParent().toFile().mkdirs();
            TomlParser.write(configPath, data);
        } catch (IOException e) {
            ShulkerSorter.LOGGER.error("Failed to save config", e);
        }
    }

    public void reset() {
        INSTANCE = new ShulkerSorterConfig();
        INSTANCE.save();
    }

    public CategoryDefinition getCategory(String key) {
        return categories.get(key);
    }
}
