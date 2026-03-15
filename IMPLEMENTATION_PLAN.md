# ShulkerSort — Implementierungsplan

> Fabric-Mod fuer Minecraft 1.21+ (spaeter Port auf MC 26.1)
> Sortiert Shulker Boxes im Inventar per Keybind nach Kategorie

---

## Inhaltsverzeichnis

1. [Ueberblick](#1-ueberblick)
2. [Projektstruktur](#2-projektstruktur)
3. [Phase 1: Projekt-Setup](#3-phase-1-projekt-setup)
4. [Phase 2: Config-System](#4-phase-2-config-system)
5. [Phase 3: Core-Utilities](#5-phase-3-core-utilities)
6. [Phase 4: Sortier-Engine](#6-phase-4-sortier-engine)
7. [Phase 5: Client-Features](#7-phase-5-client-features)
8. [Phase 6: Port auf MC 26.1](#8-phase-6-port-auf-mc-261)
9. [Mapping-Referenz](#9-mapping-referenz)
10. [Edge Cases](#10-edge-cases)
11. [Verifikation & Testing](#11-verifikation--testing)

---

## 1. Ueberblick

### Was macht die Mod?
- **Keybind druecken** (Standard: S) → Alle Shulker Boxes im Inventar werden gescannt
- **Kategorisieren** → Items werden nach Registry-ID Patterns in Kategorien eingeteilt
- **Zusammenfuehren** → Stacks werden aufgefuellt, Items in moeglichst wenige Boxen gepackt
- **Beschriften** → Jede Box bekommt automatisch einen Namen ("Bloecke #1", "Erze #2")
- **Tooltip** → Beim Hovern ueber Shulker Boxes wird der Inhalt angezeigt

### Referenzprojekt
Das existierende Projekt unter `C:\Users\mager\Downloads\inventory_shulker-template-1.21` dient als Vorlage fuer:
- Gradle-Setup und Build-Konfiguration
- Projektstruktur (Split Environment Source Sets)
- ShulkerUtils → ShulkerBoxHelper (Erkennung, Content-Zugriff)
- NotificationUtils → NotificationHelper (Chat + Sound)
- ModConfig → ShulkerSortConfig (Singleton-Pattern)

### Technologie-Stack
| Komponente | MC 1.21 | MC 26.1 |
|-----------|---------|---------|
| Java | 21 | 25 |
| Fabric Loom | 1.9-SNAPSHOT | 1.14-SNAPSHOT |
| Fabric Loader | >= 0.16.0 | >= 0.18.4 |
| Fabric API | 0.102.0+1.21 | 0.142.0+26.1 |
| Mappings | Yarn | Keine (unobfuscated) |

---

## 2. Projektstruktur

```
ShulkerSorter/
│
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
│
├── src/
│   ├── main/
│   │   ├── java/com/shulkersort/
│   │   │   ├── ShulkerSort.java                    # ModInitializer (Server/Shared)
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── TomlParser.java                  # Minimaler TOML-Parser
│   │   │   │   ├── CategoryDefinition.java          # Kategorie-Datenklasse
│   │   │   │   └── ShulkerSortConfig.java           # Config-Singleton
│   │   │   │
│   │   │   ├── sort/
│   │   │   │   ├── ItemCategorizer.java             # Item → Kategorie Zuordnung
│   │   │   │   ├── ShulkerSortEngine.java           # Kern-Sortierlogik (7 Phasen)
│   │   │   │   └── SortResult.java                  # Ergebnis-Record
│   │   │   │
│   │   │   └── util/
│   │   │       ├── ShulkerBoxHelper.java            # Shulker-Erkennung + Content R/W
│   │   │       └── NotificationHelper.java          # Chat-Nachrichten + Sound
│   │   │
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       ├── shulkersort.mixins.json              # Leer (keine Server-Mixins)
│   │       └── assets/shulkersort/
│   │           ├── icon.png
│   │           └── lang/
│   │               ├── de_de.json
│   │               └── en_us.json
│   │
│   └── client/
│       ├── java/com/shulkersort/
│       │   ├── ShulkerSortClient.java               # ClientModInitializer
│       │   ├── keybind/
│       │   │   └── SortKeybindHandler.java          # Keybind + Tick-Handler
│       │   ├── tooltip/
│       │   │   └── ShulkerTooltipRenderer.java      # Tooltip-Inhalt
│       │   └── hud/
│       │       └── SortingHudOverlay.java           # "Sortiere..." Animation
│       │
│       └── resources/
│           └── shulkersort.client.mixins.json       # Leer (Tooltip via Fabric API)
│
├── Mod.md                                           # Original-Spezifikation
└── IMPLEMENTATION_PLAN.md                           # Diese Datei
```

---

## 3. Phase 1: Projekt-Setup

### 3.1 gradle.properties

```properties
# Gradle
org.gradle.jvmargs=-Xmx1G
org.gradle.parallel=true

# Fabric - MC 1.21
minecraft_version=1.21
yarn_mappings=1.21+build.9
loader_version=0.16.14
fabric_version=0.102.0+1.21

# Mod
mod_version=1.0.0
maven_group=com.shulkersort
archives_base_name=shulkersort
```

### 3.2 settings.gradle

```groovy
pluginManagement {
    repositories {
        maven { url "https://maven.fabricmc.net/" }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

### 3.3 build.gradle

```groovy
plugins {
    id 'fabric-loom' version '1.9-SNAPSHOT'
    id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

// Split client/server Source Sets (wie Referenzprojekt)
loom {
    splitEnvironmentSourceSets()
    mods {
        "shulkersort" {
            sourceSet sourceSets.main
            sourceSet sourceSets.client
        }
    }
}

repositories {
    // Keine zusaetzlichen Repos noetig
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
}

processResources {
    inputs.property "version", project.version
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = 21
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
```

### 3.4 fabric.mod.json

```json
{
    "schemaVersion": 1,
    "id": "shulkersort",
    "version": "${version}",
    "name": "ShulkerSort",
    "description": "Sort and merge shulker box contents by category with a single keybind.",
    "authors": ["DennisTheGamer"],
    "license": "MIT",
    "icon": "assets/shulkersort/icon.png",
    "environment": "client",
    "entrypoints": {
        "main": ["com.shulkersort.ShulkerSort"],
        "client": ["com.shulkersort.ShulkerSortClient"]
    },
    "mixins": [
        "shulkersort.mixins.json"
    ],
    "depends": {
        "fabricloader": ">=0.16.0",
        "minecraft": ">=1.21 <=1.21.4",
        "java": ">=21",
        "fabric-api": "*"
    }
}
```

### 3.5 shulkersort.mixins.json (leer, Platzhalter)

```json
{
    "required": true,
    "package": "com.shulkersort.mixin",
    "compatibilityLevel": "JAVA_21",
    "mixins": [],
    "client": [],
    "injectors": {
        "defaultRequire": 1
    }
}
```

### 3.6 ShulkerSort.java (Main Entrypoint)

```java
package com.shulkersort;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerSort implements ModInitializer {
    public static final String MOD_ID = "shulkersort";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("ShulkerSort initialized!");
    }
}
```

### 3.7 ShulkerSortClient.java (Client Entrypoint)

```java
package com.shulkersort;

import com.shulkersort.config.ShulkerSortConfig;
import com.shulkersort.keybind.SortKeybindHandler;
import com.shulkersort.hud.SortingHudOverlay;
import com.shulkersort.tooltip.ShulkerTooltipRenderer;
import com.shulkersort.util.ShulkerBoxHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class ShulkerSortClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ShulkerSortConfig.getInstance(); // Config laden

        SortKeybindHandler.register();
        SortingHudOverlay.register();

        // Tooltip via Fabric API Event (kein Mixin noetig)
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!ShulkerSortConfig.getInstance().tooltipEnabled) return;
            if (!ShulkerBoxHelper.isShulkerBox(stack)) return;
            ShulkerTooltipRenderer.appendTooltip(stack, lines);
        });

        ShulkerSort.LOGGER.info("ShulkerSort client initialized!");
    }
}
```

### 3.8 Gradle Wrapper

Kopiere folgende Dateien vom Referenzprojekt:
- `gradlew` (Unix)
- `gradlew.bat` (Windows)
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

### Verifikation Phase 1
```bash
./gradlew build
```
Erwartung: Kompiliert ohne Fehler. Mod laesst sich in MC 1.21 laden.

---

## 4. Phase 2: Config-System

### 4.1 CategoryDefinition.java

```java
package com.shulkersort.config;

import java.util.List;

public class CategoryDefinition {
    private final String key;
    private final String labelPrefix;
    private final List<String> patterns;

    public CategoryDefinition(String key, String labelPrefix, List<String> patterns) {
        this.key = key;
        this.labelPrefix = labelPrefix;
        this.patterns = patterns;
    }

    public String getKey() { return key; }
    public String getLabelPrefix() { return labelPrefix; }
    public List<String> getPatterns() { return patterns; }
}
```

### 4.2 TomlParser.java

Minimaler Parser der folgende TOML-Features unterstuetzt:
- `key = "string"` (Strings in Anfuehrungszeichen)
- `key = true/false` (Booleans)
- `key = 42` (Integers)
- `key = ["a", "b", "c"]` (String-Arrays)
- `[section]` und `[section.subsection]` (Tables)
- `# Kommentar` (Zeilen-Kommentare)

**Kernmethoden:**
```java
public class TomlParser {
    // Liest TOML-Datei in verschachtelte Map
    public static Map<String, Object> parse(Path filePath);

    // Schreibt Map zurueck als TOML mit Kommentaren
    public static void write(Path filePath, Map<String, Object> data);
}
```

**Implementierungshinweis:** ~150-200 Zeilen. Zeilenweises Parsing. Keine externen Abhaengigkeiten.

### 4.3 ShulkerSortConfig.java

```java
package com.shulkersort.config;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
import java.util.*;

public class ShulkerSortConfig {
    private static ShulkerSortConfig INSTANCE;
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("shulkersort.toml");

    // === Einstellungen ===
    public String keybind = "key.keyboard.s";
    public boolean autoLabel = true;
    public boolean tooltipEnabled = true;
    public int tooltipMaxLines = 5;
    public String lockedTag = "[LOCKED]";
    public List<String> categoryOrder = new ArrayList<>(List.of(
        "blocks", "tools", "food", "ores", "brewing", "misc"
    ));
    public Map<String, CategoryDefinition> categories = buildDefaults();

    // === Singleton ===
    public static ShulkerSortConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ShulkerSortConfig load() { /* TOML einlesen */ }
    public void save() { /* TOML schreiben */ }

    // === Default-Kategorien ===
    private static Map<String, CategoryDefinition> buildDefaults() {
        Map<String, CategoryDefinition> map = new LinkedHashMap<>();

        map.put("blocks", new CategoryDefinition("blocks", "Bloecke", List.of(
            "_log", "_planks", "_stone", "_brick", "_slab", "_stairs",
            "_wall", "_fence", "_gate", "sand", "gravel", "dirt",
            "glass", "_concrete", "_terracotta", "_wool", "_carpet"
        )));

        map.put("tools", new CategoryDefinition("tools", "Werkzeuge", List.of(
            "_sword", "_pickaxe", "_axe", "_shovel", "_hoe",
            "_helmet", "_chestplate", "_leggings", "_boots",
            "bow", "crossbow", "shield", "trident", "fishing_rod", "shears"
        )));

        map.put("food", new CategoryDefinition("food", "Nahrung", List.of(
            "_seeds", "_sapling", "_flower", "apple", "bread",
            "carrot", "potato", "beetroot", "melon_slice", "pumpkin",
            "sweet_berries", "cocoa_beans", "wheat", "sugar_cane"
        )));
        // Hinweis: Items mit Food-Component werden automatisch hier zugeordnet

        map.put("ores", new CategoryDefinition("ores", "Erze", List.of(
            "_ore", "_ingot", "_nugget", "raw_", "_gem",
            "coal", "quartz", "ancient_debris", "netherite_scrap",
            "diamond", "emerald", "lapis_lazuli", "redstone", "amethyst_shard"
        )));

        map.put("brewing", new CategoryDefinition("brewing", "Brauen", List.of(
            "potion", "splash_potion", "lingering_potion",
            "brewing_stand", "_dye", "glass_bottle", "blaze_powder",
            "nether_wart", "ghast_tear", "magma_cream",
            "fermented_spider_eye", "glistering_melon_slice"
        )));

        map.put("misc", new CategoryDefinition("misc", "Sonstiges", List.of()));

        return map;
    }
}
```

### 4.4 Default shulkersort.toml (wird automatisch generiert)

```toml
# ShulkerSort Konfiguration

keybind = "key.keyboard.s"
auto_label = true
tooltip_enabled = true
tooltip_max_lines = 5
locked_tag = "[LOCKED]"

category_order = ["blocks", "tools", "food", "ores", "brewing", "misc"]

[categories.blocks]
label = "Bloecke"
patterns = ["_log", "_planks", "_stone", "_brick", "_slab", "_stairs", "_wall", "_fence", "_gate", "sand", "gravel", "dirt", "glass", "_concrete", "_terracotta", "_wool", "_carpet"]

[categories.tools]
label = "Werkzeuge"
patterns = ["_sword", "_pickaxe", "_axe", "_shovel", "_hoe", "_helmet", "_chestplate", "_leggings", "_boots", "bow", "crossbow", "shield", "trident", "fishing_rod", "shears"]

[categories.food]
label = "Nahrung"
# Items mit Food-Component werden automatisch dieser Kategorie zugeordnet
patterns = ["_seeds", "_sapling", "_flower", "apple", "bread", "carrot", "potato", "beetroot", "melon_slice", "pumpkin", "sweet_berries", "cocoa_beans", "wheat", "sugar_cane"]

[categories.ores]
label = "Erze"
patterns = ["_ore", "_ingot", "_nugget", "raw_", "_gem", "coal", "quartz", "ancient_debris", "netherite_scrap", "diamond", "emerald", "lapis_lazuli", "redstone", "amethyst_shard"]

[categories.brewing]
label = "Brauen"
patterns = ["potion", "splash_potion", "lingering_potion", "brewing_stand", "_dye", "glass_bottle", "blaze_powder", "nether_wart", "ghast_tear", "magma_cream", "fermented_spider_eye"]

[categories.misc]
label = "Sonstiges"
patterns = []
```

### Verifikation Phase 2
- Config-Datei wird beim Mod-Start erstellt wenn nicht vorhanden
- Aendern der TOML → Aenderungen werden nach Neustart uebernommen

---

## 5. Phase 3: Core-Utilities

### 5.1 ShulkerBoxHelper.java

Basiert auf `ShulkerUtils.java` aus dem Referenzprojekt, adaptiert fuer **Yarn Mappings (MC 1.21)**.

```java
package com.shulkersort.util;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import java.util.ArrayList;
import java.util.List;

public class ShulkerBoxHelper {

    /** Prueft ob der ItemStack eine Shulker Box ist */
    public static boolean isShulkerBox(ItemStack stack) {
        return !stack.isEmpty()
            && stack.getItem() instanceof BlockItem blockItem
            && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    /** Liest den Inhalt einer Shulker Box (27 Slots) */
    public static List<ItemStack> getContents(ItemStack shulkerStack) {
        if (!isShulkerBox(shulkerStack)) return List.of();

        ContainerComponent container = shulkerStack.getOrDefault(
            DataComponentTypes.CONTAINER,
            ContainerComponent.DEFAULT
        );

        // In eine modifizierbare Liste kopieren
        DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
        container.copyTo(items);

        List<ItemStack> result = new ArrayList<>();
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                result.add(item.copy());
            }
        }
        return result;
    }

    /** Setzt den Inhalt einer Shulker Box */
    public static void setContents(ItemStack shulkerStack, List<ItemStack> items) {
        if (!isShulkerBox(shulkerStack)) return;
        ContainerComponent newContents = ContainerComponent.fromStacks(items);
        shulkerStack.set(DataComponentTypes.CONTAINER, newContents);
    }

    /** Prueft ob die Box leer ist */
    public static boolean isEmpty(ItemStack shulkerStack) {
        return getContents(shulkerStack).isEmpty();
    }

    /** Prueft ob die Box den Locked-Tag im Custom Name hat */
    public static boolean isLocked(ItemStack shulkerStack, String lockedTag) {
        Text name = shulkerStack.get(DataComponentTypes.CUSTOM_NAME);
        if (name == null) return false;
        return name.getString().contains(lockedTag);
    }

    /** Setzt den Custom Name einer Shulker Box */
    public static void setCustomName(ItemStack shulkerStack, String name) {
        shulkerStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
    }

    /** Entfernt den Custom Name */
    public static void removeCustomName(ItemStack shulkerStack) {
        shulkerStack.remove(DataComponentTypes.CUSTOM_NAME);
    }
}
```

### 5.2 NotificationHelper.java

```java
package com.shulkersort.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NotificationHelper {

    /** Gruene Erfolgsmeldung + Pling-Sound */
    public static void sendSuccess(PlayerEntity player, int boxCount) {
        Text msg = Text.literal("ShulkerSort: " + boxCount + " Boxen sortiert.")
            .formatted(Formatting.GREEN);
        player.sendMessage(msg, false);

        // "Pling"-Sound (Experience Orb Pickup)
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    /** Rote Fehlermeldung */
    public static void sendError(PlayerEntity player, String message) {
        Text msg = Text.literal("ShulkerSort: " + message)
            .formatted(Formatting.RED);
        player.sendMessage(msg, false);
    }

    /** Gelbe Info-Meldung */
    public static void sendInfo(PlayerEntity player, String message) {
        Text msg = Text.literal("ShulkerSort: " + message)
            .formatted(Formatting.YELLOW);
        player.sendMessage(msg, false);
    }
}
```

---

## 6. Phase 4: Sortier-Engine

### 6.1 SortResult.java

```java
package com.shulkersort.sort;

public record SortResult(
    boolean success,
    int boxesSorted,
    int itemsMoved,
    String errorMessage // null bei Erfolg
) {
    public static SortResult success(int boxes, int items) {
        return new SortResult(true, boxes, items, null);
    }

    public static SortResult error(String message) {
        return new SortResult(false, 0, 0, message);
    }
}
```

### 6.2 ItemCategorizer.java

```java
package com.shulkersort.sort;

import com.shulkersort.config.CategoryDefinition;
import com.shulkersort.config.ShulkerSortConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import java.util.*;

public class ItemCategorizer {
    private final ShulkerSortConfig config;

    public ItemCategorizer(ShulkerSortConfig config) {
        this.config = config;
    }

    /**
     * Ordnet einen einzelnen ItemStack einer Kategorie zu.
     * Gibt den Kategorie-Key zurueck (z.B. "blocks", "tools", "misc").
     */
    public String categorize(ItemStack stack) {
        // Registry-ID holen (z.B. "oak_planks", "diamond_sword")
        String id = Registries.ITEM.getId(stack.getItem()).getPath();

        // Sonderfall: Food-Component → automatisch "food"
        if (config.categories.containsKey("food")
            && stack.contains(DataComponentTypes.FOOD)) {
            return "food";
        }

        // Kategorien in konfigurierter Reihenfolge pruefen
        for (String catKey : config.categoryOrder) {
            if (catKey.equals("misc")) continue; // misc ist Fallback

            CategoryDefinition cat = config.categories.get(catKey);
            if (cat == null) continue;

            for (String pattern : cat.getPatterns()) {
                if (id.contains(pattern) || id.equals(pattern)) {
                    return catKey;
                }
            }
        }

        return "misc"; // Fallback
    }

    /**
     * Kategorisiert eine Liste von Items.
     * Gibt Map<KategorieKey, List<ItemStack>> zurueck.
     */
    public Map<String, List<ItemStack>> categorizeAll(List<ItemStack> items) {
        Map<String, List<ItemStack>> result = new LinkedHashMap<>();
        for (String cat : config.categoryOrder) {
            result.put(cat, new ArrayList<>());
        }

        for (ItemStack item : items) {
            String cat = categorize(item);
            result.computeIfAbsent(cat, k -> new ArrayList<>()).add(item);
        }

        // Leere Kategorien entfernen
        result.entrySet().removeIf(e -> e.getValue().isEmpty());
        return result;
    }
}
```

### 6.3 ShulkerSortEngine.java — Der 7-Phasen-Algorithmus

Dies ist die **komplexeste Klasse** der Mod. Der Algorithmus arbeitet in 7 Phasen:

```java
package com.shulkersort.sort;

import com.shulkersort.config.ShulkerSortConfig;
import com.shulkersort.util.ShulkerBoxHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import java.util.*;

public class ShulkerSortEngine {
    private final ShulkerSortConfig config;
    private final ItemCategorizer categorizer;

    public ShulkerSortEngine(ShulkerSortConfig config) {
        this.config = config;
        this.categorizer = new ItemCategorizer(config);
    }

    public SortResult sort(PlayerInventory inventory) {
        // ============================================
        // PHASE 1: SCAN
        // ============================================
        // Finde alle Shulker Boxes in Slots 0-35
        // Trenne: locked, leer, sortierbar

        List<Integer> sortableSlots = new ArrayList<>();    // Slot-Indices mit sortierbaren Boxen
        List<ItemStack> allItems = new ArrayList<>();       // Alle extrahierten Items
        int lockedCount = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!ShulkerBoxHelper.isShulkerBox(stack)) continue;
            if (ShulkerBoxHelper.isLocked(stack, config.lockedTag)) {
                lockedCount++;
                continue;
            }
            // Auch leere Boxen sind "sortierbar" (koennen befuellt werden)
            sortableSlots.add(i);
            allItems.addAll(ShulkerBoxHelper.getContents(stack));
        }

        // Keine sortierbaren Boxen?
        if (sortableSlots.isEmpty()) {
            if (lockedCount > 0) {
                return SortResult.error("Alle Shulker Boxes sind gesperrt!");
            }
            return SortResult.error("Keine Shulker Boxes im Inventar!");
        }

        // Keine Items zum Sortieren?
        if (allItems.isEmpty()) {
            return SortResult.error("Alle Shulker Boxes sind leer.");
        }

        // ============================================
        // PHASE 2: CATEGORIZE
        // ============================================
        Map<String, List<ItemStack>> categorized = categorizer.categorizeAll(allItems);

        // ============================================
        // PHASE 3: MERGE STACKS
        // ============================================
        // Fuer jede Kategorie: gleiche Items zusammenfuehren
        Map<String, List<ItemStack>> merged = new LinkedHashMap<>();
        for (var entry : categorized.entrySet()) {
            merged.put(entry.getKey(), mergeStacks(entry.getValue()));
        }

        // ============================================
        // PHASE 4: VALIDATE (Kapazitaetspruefung)
        // ============================================
        int totalStacks = merged.values().stream()
            .mapToInt(List::size).sum();
        int totalSlots = sortableSlots.size() * 27;

        if (totalStacks > totalSlots) {
            return SortResult.error(
                "Nicht genug Shulker Boxes fuer alle Items! Sortierung abgebrochen."
            );
        }

        // ============================================
        // PHASE 5: DISTRIBUTE
        // ============================================
        // Weise Items den Boxen zu (in-memory, noch keine Aenderung!)
        // Map: SlotIndex → neue Box-Inhalte (List<ItemStack> mit max 27 Eintraegen)
        Map<Integer, List<ItemStack>> newContents = new LinkedHashMap<>();
        int boxIndex = 0;

        for (String catKey : config.categoryOrder) {
            List<ItemStack> items = merged.get(catKey);
            if (items == null || items.isEmpty()) continue;

            int itemIndex = 0;
            while (itemIndex < items.size()) {
                if (boxIndex >= sortableSlots.size()) {
                    // Sollte durch Phase 4 verhindert werden
                    return SortResult.error("Interner Fehler: Nicht genug Boxen.");
                }

                int slot = sortableSlots.get(boxIndex);
                List<ItemStack> boxItems = new ArrayList<>();

                // Box fuellen (max 27 Slots)
                while (itemIndex < items.size() && boxItems.size() < 27) {
                    boxItems.add(items.get(itemIndex));
                    itemIndex++;
                }

                newContents.put(slot, boxItems);
                boxIndex++;
            }
        }

        // Verbleibende Boxen leeren
        while (boxIndex < sortableSlots.size()) {
            newContents.put(sortableSlots.get(boxIndex), List.of());
            boxIndex++;
        }

        // ============================================
        // PHASE 6: AUTO-LABEL
        // ============================================
        // Map: SlotIndex → neuer Name (oder null)
        Map<Integer, String> newLabels = new LinkedHashMap<>();

        if (config.autoLabel) {
            // Zaehler pro Kategorie
            Map<String, Integer> categoryCounter = new LinkedHashMap<>();
            boxIndex = 0;

            for (String catKey : config.categoryOrder) {
                List<ItemStack> items = merged.get(catKey);
                if (items == null || items.isEmpty()) continue;

                var catDef = config.categories.get(catKey);
                String prefix = (catDef != null) ? catDef.getLabelPrefix() : catKey;
                int counter = 1;
                int itemIndex = 0;

                while (itemIndex < items.size()) {
                    int slot = sortableSlots.get(boxIndex);
                    newLabels.put(slot, prefix + " #" + counter);
                    counter++;

                    // Wie viele Items in dieser Box?
                    int inThisBox = Math.min(27, items.size() - itemIndex);
                    itemIndex += inThisBox;
                    boxIndex++;
                }
            }

            // Leere Boxen: Label entfernen
            while (boxIndex < sortableSlots.size()) {
                newLabels.put(sortableSlots.get(boxIndex), null); // null = entfernen
                boxIndex++;
            }
        }

        // ============================================
        // PHASE 7: APPLY (Atomares Commit)
        // ============================================
        int boxesSorted = 0;
        for (var entry : newContents.entrySet()) {
            int slot = entry.getKey();
            ItemStack shulkerStack = inventory.getStack(slot);

            // Inhalt setzen
            ShulkerBoxHelper.setContents(shulkerStack, entry.getValue());

            // Label setzen
            if (config.autoLabel && newLabels.containsKey(slot)) {
                String label = newLabels.get(slot);
                if (label != null) {
                    ShulkerBoxHelper.setCustomName(shulkerStack, label);
                } else {
                    ShulkerBoxHelper.removeCustomName(shulkerStack);
                }
            }

            // Stack im Inventar aktualisieren (triggert Sync)
            inventory.setStack(slot, shulkerStack);
            boxesSorted++;
        }

        inventory.markDirty();
        return SortResult.success(boxesSorted, allItems.size());
    }

    /**
     * Fuehrt gleiche Items zusammen und fuellt Stacks auf.
     * Beispiel: 3x Cobblestone(64) + 1x Cobblestone(30)
     *        → 3x Cobblestone(64) + 1x Cobblestone(30) [schon optimal]
     * Beispiel: 2x Cobblestone(40) → 1x Cobblestone(64) + 1x Cobblestone(16)
     */
    private List<ItemStack> mergeStacks(List<ItemStack> items) {
        List<ItemStack> merged = new ArrayList<>();

        for (ItemStack item : items) {
            if (item.isEmpty()) continue;
            int remaining = item.getCount();

            // Versuche in bestehende Stacks einzufuellen
            for (ItemStack existing : merged) {
                if (remaining <= 0) break;
                if (ItemStack.areItemsAndComponentsEqual(existing, item)
                    && existing.getCount() < existing.getMaxCount()) {

                    int transfer = Math.min(remaining,
                        existing.getMaxCount() - existing.getCount());
                    existing.increment(transfer);
                    remaining -= transfer;
                }
            }

            // Rest als neuen Stack hinzufuegen
            if (remaining > 0) {
                ItemStack newStack = item.copy();
                newStack.setCount(remaining);
                merged.add(newStack);
            }
        }

        return merged;
    }
}
```

### Algorithmus-Visualisierung

```
Inventar vorher:
  [Shulker A: cobblestone(64), dirt(30), diamond(5)]
  [Shulker B: cobblestone(40), oak_log(64)]
  [Shulker C: diamond_sword(1), iron_ingot(20)]

Phase 1 SCAN: Alle Items extrahieren
  → cobblestone(64), dirt(30), diamond(5), cobblestone(40), oak_log(64),
    diamond_sword(1), iron_ingot(20)

Phase 2 CATEGORIZE:
  blocks: cobblestone(64), dirt(30), cobblestone(40), oak_log(64)
  tools:  diamond_sword(1)
  ores:   diamond(5), iron_ingot(20)

Phase 3 MERGE:
  blocks: cobblestone(64), cobblestone(40), dirt(30), oak_log(64)
        → cobblestone(64), cobblestone(40), dirt(30), oak_log(64) [schon optimal]
  tools:  diamond_sword(1) [nicht stackbar]
  ores:   diamond(5), iron_ingot(20)

Phase 4 VALIDATE:
  Benoetigt: 4 + 1 + 2 = 7 Slots, Verfuegbar: 3 * 27 = 81 → OK

Phase 5 DISTRIBUTE:
  Shulker A → blocks: cobblestone(64), cobblestone(40), dirt(30), oak_log(64)
  Shulker B → tools: diamond_sword(1)
              ores: diamond(5), iron_ingot(20)
  Shulker C → (leer)

  Hinweis: Tools und Ores passen zusammen in eine Box.
  KORREKTUR: Jede Kategorie startet in einer neuen Box!
  Shulker A → blocks: cobblestone(64), cobblestone(40), dirt(30), oak_log(64)
  Shulker B → tools: diamond_sword(1)
  Shulker C → ores: diamond(5), iron_ingot(20)

Phase 6 LABEL:
  Shulker A → "Bloecke #1"
  Shulker B → "Werkzeuge #1"
  Shulker C → "Erze #1"

Phase 7 APPLY: Alles auf einmal anwenden.
```

**Wichtig:** Der obige Code startet jede Kategorie in einer neuen Shulker Box. Das ist das intuitivere Verhalten. Alternativ koennte man Kategorien in einer Box mischen um Platz zu sparen — dies ist aber im Spec nicht vorgesehen.

---

## 7. Phase 5: Client-Features

### 7.1 SortKeybindHandler.java

```java
package com.shulkersort.keybind;

import com.shulkersort.config.ShulkerSortConfig;
import com.shulkersort.hud.SortingHudOverlay;
import com.shulkersort.sort.ShulkerSortEngine;
import com.shulkersort.sort.SortResult;
import com.shulkersort.util.NotificationHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SortKeybindHandler {
    private static KeyBinding sortKey;
    private static boolean sorting = false;

    public static void register() {
        sortKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.shulkersort.sort",        // Translation Key
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_S,               // Default: S (wie Spec)
            "category.shulkersort"          // Keybind-Kategorie
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (sortKey.wasPressed() && !sorting) {
                performSort(client);
            }
        });
    }

    private static void performSort(MinecraftClient client) {
        if (client.player == null) return;
        if (client.currentScreen != null) return; // Nicht waehrend GUI offen

        sorting = true;
        SortingHudOverlay.show();

        ShulkerSortEngine engine = new ShulkerSortEngine(
            ShulkerSortConfig.getInstance()
        );

        SortResult result = engine.sort(client.player.getInventory());

        if (result.success()) {
            NotificationHelper.sendSuccess(client.player, result.boxesSorted());
        } else {
            NotificationHelper.sendError(client.player, result.errorMessage());
        }

        SortingHudOverlay.hide();
        sorting = false;
    }
}
```

**Achtung S-Taste:** Die S-Taste ist standardmaessig "Rueckwaerts gehen". Der Spieler sollte darauf hingewiesen werden, den Keybind in den Einstellungen zu aendern. `wasPressed()` konsumiert den Tastendruck, aber es wird trotzdem ein Konflikt mit der Bewegung geben. Empfehlung: In der Config-Datei einen Kommentar hinzufuegen.

### 7.2 ShulkerTooltipRenderer.java

```java
package com.shulkersort.tooltip;

import com.shulkersort.config.ShulkerSortConfig;
import com.shulkersort.util.ShulkerBoxHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.*;

public class ShulkerTooltipRenderer {

    public static void appendTooltip(ItemStack shulkerStack, List<Text> lines) {
        List<ItemStack> contents = ShulkerBoxHelper.getContents(shulkerStack);

        if (contents.isEmpty()) {
            lines.add(Text.literal("  Leer").formatted(Formatting.GRAY));
            return;
        }

        // Gleiche Items zusammenzaehlen fuer Anzeige
        // LinkedHashMap behaelt Reihenfolge bei
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();

        for (ItemStack item : contents) {
            String key = Registries.ITEM.getId(item.getItem()).toString();
            counts.merge(key, item.getCount(), Integer::sum);
            names.putIfAbsent(key, item.getName().getString());
        }

        int maxLines = ShulkerSortConfig.getInstance().tooltipMaxLines;
        int shown = 0;
        int remaining = 0;

        for (var entry : counts.entrySet()) {
            if (shown < maxLines) {
                String name = names.get(entry.getKey());
                int count = entry.getValue();
                lines.add(Text.literal("  " + name + " x" + count)
                    .formatted(Formatting.GRAY));
                shown++;
            } else {
                remaining++;
            }
        }

        if (remaining > 0) {
            lines.add(Text.literal("  ... und " + remaining + " weitere Items")
                .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        }
    }
}
```

### 7.3 SortingHudOverlay.java

```java
package com.shulkersort.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SortingHudOverlay {
    private static boolean visible = false;
    private static long showTime = 0;
    private static final long DISPLAY_MS = 1000; // Max 1 Sekunde

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            if (!visible) return;

            long elapsed = System.currentTimeMillis() - showTime;
            if (elapsed > DISPLAY_MS) {
                visible = false;
                return;
            }

            renderOverlay(drawContext, elapsed);
        });
    }

    private static void renderOverlay(DrawContext drawContext, long elapsed) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();

        // Animierte Punkte: "Sortiere." → "Sortiere.." → "Sortiere..."
        int dots = (int) ((elapsed / 300) % 3) + 1;
        String text = "Sortiere" + ".".repeat(dots);

        // Zentriert oben zeichnen
        int textWidth = client.textRenderer.getWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = 10;

        drawContext.drawText(client.textRenderer, text, x, y, 0xFFFFFF00, true);
    }

    public static void show() {
        visible = true;
        showTime = System.currentTimeMillis();
    }

    public static void hide() {
        visible = false;
    }
}
```

### 7.4 Sprach-Dateien

**de_de.json:**
```json
{
    "key.shulkersort.sort": "Shulker Sortieren",
    "category.shulkersort": "ShulkerSort"
}
```

**en_us.json:**
```json
{
    "key.shulkersort.sort": "Sort Shulkers",
    "category.shulkersort": "ShulkerSort"
}
```

---

## 8. Phase 6: Port auf MC 26.1

Nach erfolgreicher 1.21-Implementierung:

### 8.1 Build-Konfiguration aendern

**gradle.properties:**
```properties
minecraft_version=26.1-snapshot-3
loader_version=0.18.4
fabric_version=0.142.0+26.1
# yarn_mappings entfernen (nicht noetig)
```

**build.gradle:**
```groovy
// ENTFERNEN:
// mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"

// Java-Version aendern:
tasks.withType(JavaCompile).configureEach {
    it.options.release = 25
}
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
```

### 8.2 Mapping-Rename (Search & Replace)

Siehe [Mapping-Referenz](#9-mapping-referenz) unten.

### 8.3 Mixin-Config aendern

```json
"compatibilityLevel": "JAVA_25"
```

### 8.4 fabric.mod.json aendern

```json
"depends": {
    "fabricloader": ">=0.18.4",
    "minecraft": ">=26.1-alpha.3",
    "java": ">=25"
}
```

---

## 9. Mapping-Referenz

### Yarn (MC 1.21) → Mojang (MC 26.1) Umbenennungen

| Yarn (MC 1.21) | Mojang (MC 26.1) |
|---|---|
| `net.minecraft.component.DataComponentTypes` | `net.minecraft.core.component.DataComponents` |
| `net.minecraft.component.type.ContainerComponent` | `net.minecraft.world.item.component.ItemContainerContents` |
| `net.minecraft.text.Text` | `net.minecraft.network.chat.Component` |
| `net.minecraft.entity.player.PlayerInventory` | `net.minecraft.world.entity.player.Inventory` |
| `net.minecraft.entity.player.PlayerEntity` | `net.minecraft.world.entity.player.Player` |
| `net.minecraft.item.ItemStack` | `net.minecraft.world.item.ItemStack` |
| `net.minecraft.item.BlockItem` | `net.minecraft.world.item.BlockItem` |
| `net.minecraft.block.ShulkerBoxBlock` | `net.minecraft.world.level.block.ShulkerBoxBlock` |
| `net.minecraft.sound.SoundEvents` | `net.minecraft.sounds.SoundEvents` |
| `net.minecraft.util.Formatting` | `net.minecraft.ChatFormatting` |
| `net.minecraft.registry.Registries` | `net.minecraft.core.registries.BuiltInRegistries` |
| `net.minecraft.util.collection.DefaultedList` | `net.minecraft.core.NonNullList` |

### Methoden-Mapping

| Yarn (MC 1.21) | Mojang (MC 26.1) |
|---|---|
| `ItemStack.areItemsAndComponentsEqual()` | `ItemStack.isSameItemSameComponents()` |
| `ContainerComponent.fromStacks()` | `ItemContainerContents.fromItems()` |
| `ContainerComponent.copyTo()` | `ItemContainerContents.copyInto()` |
| `ContainerComponent.DEFAULT` | `ItemContainerContents.EMPTY` |
| `PlayerInventory.getStack()` | `Inventory.getItem()` |
| `PlayerInventory.setStack()` | `Inventory.setItem()` |
| `PlayerEntity.sendMessage()` | `Player.displayClientMessage()` |
| `PlayerEntity.getWorld()` | `Player.level()` |
| `Text.literal()` | `Component.literal()` |
| `ItemStack.getName()` | `ItemStack.getHoverName()` |

---

## 10. Edge Cases

| Fall | Verhalten |
|------|-----------|
| Keine Shulker Boxes im Inventar | Fehlermeldung, nichts passiert |
| Alle Boxen leer | Fehlermeldung, nichts passiert |
| Alle Boxen gesperrt [LOCKED] | Fehlermeldung, nichts passiert |
| Zu wenig Boxen fuer alle Items | Fehlermeldung, **nichts** wird veraendert (atomar) |
| Nicht-stackbare Items (Werkzeuge, Ruestung) | Jedes Item nimmt 1 Slot ein |
| Verzauberte Items | Werden als eigene Stacks behandelt (verschiedene Components) |
| Items aus Mods | Werden in "misc" einsortiert (oder per Config einer Kategorie zugewiesen) |
| Config-Datei fehlt/korrupt | Default-Werte verwenden, neue Config schreiben |
| Shulker Box in Shulker Box | Nicht moeglich in Vanilla, aber zur Sicherheit ignorieren |
| Sortierung waehrend Screen offen | Keybind wird ignoriert |

---

## 11. Verifikation & Testing

### Checkliste

- [ ] `./gradlew build` kompiliert ohne Fehler
- [ ] Mod erscheint im Fabric Mod-Menu in MC 1.21
- [ ] `config/shulkersort.toml` wird beim ersten Start generiert
- [ ] Config-Werte werden korrekt geladen und angewendet
- [ ] Keybind erscheint in den Steuerungs-Einstellungen unter "ShulkerSort"
- [ ] Sortierung mit gemischten Items funktioniert korrekt
- [ ] Stacks werden korrekt zusammengefuehrt (z.B. 2x30 Cobblestone → 1x60)
- [ ] Jede Kategorie startet in einer neuen Box
- [ ] Auto-Labeling: Boxen werden beschriftet ("Bloecke #1", "Erze #1")
- [ ] [LOCKED]-Boxen werden uebersprungen
- [ ] Leere Boxen nach Sortierung bleiben im Inventar
- [ ] Tooltip zeigt Inhalt beim Hovern (max 5 Zeilen + "... und X weitere")
- [ ] Gruene Erfolgsmeldung + Pling-Sound nach Sortierung
- [ ] Rote Fehlermeldung bei zu wenig Boxen
- [ ] "Sortiere..." HUD-Animation waehrend der Sortierung
- [ ] Atomaritaet: Bei Fehler werden keine Aenderungen vorgenommen

### Test-Szenarien

**Szenario 1: Basis-Sortierung**
1. 3 Shulker Boxes mit gemischten Items (Bloecke + Erze + Nahrung)
2. Keybind druecken
3. Erwartung: Items nach Kategorie sortiert, Boxen beschriftet

**Szenario 2: Locked Box**
1. 3 Boxes, eine davon benannt "[LOCKED] Wichtig"
2. Keybind druecken
3. Erwartung: Locked Box unberuehrt, nur 2 Boxen sortiert

**Szenario 3: Overflow**
1. 2 Boxen voll mit Items aus 5+ Kategorien
2. Keybind druecken
3. Erwartung: Rote Fehlermeldung, nichts veraendert

**Szenario 4: Stack-Merging**
1. Box A: 40x Cobblestone, Box B: 40x Cobblestone
2. Keybind druecken
3. Erwartung: Box A: 64x Cobblestone + 16x Cobblestone, Box B: leer

---

*Erstellt am 2026-03-11 | Ziel: MC 1.21 → MC 26.1*
