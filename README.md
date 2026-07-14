# ShulkerSorter

A Fabric and NeoForge mod that automatically sorts, merges and labels shulker boxes in your inventory by item category. Works in singleplayer, multiplayer survival (mod on server), and multiplayer creative.

![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric%20%7C%20NeoForge-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Version](https://img.shields.io/badge/Version-1.2.0-orange)

## Supported Minecraft Versions

| Branch | Minecraft | Java |
|--------|-----------|------|
| mc1.21-1.21.8 | 1.21 – 1.21.8 | 21+ |
| mc1.21.9-1.21.11 | 1.21.9 – 1.21.11 | 21+ |
| mc26.1 | 26.1+ | 25+ |
| mc26.2 | 26.2+ | 25+ |

## Features

### Sorting Engine
- Press **J** to sort all shulker boxes in your inventory
- **11 categories** — Redstone, Transport, Nature, Mob Loot, Decoration, Blocks, Tools, Food, Ores, Brewing, Misc
- Partial stacks of the same item are merged automatically
- Affinity-based distribution — boxes keep their dominant category when re-sorting
- Two-pass pattern matching — specific patterns (prefix/suffix) checked before substring matches
- Validates capacity before sorting — no items are ever lost

### Overflow Modes
- **Fill** — Pack items across available boxes when a category has no dedicated box
- **Dominant** — Each box is dedicated to one category only

### Multiplayer Support
- **Singleplayer** — Sorting runs on the integrated server thread
- **Multiplayer survival** — Server-side sorting via custom networking packets (mod required on server)
- **Multiplayer creative** — Client-side sort with creative packet sync (no server mod needed)
- **Graceful fallback** — Clear message when server doesn't have the mod

### Undo
- Press **Shift+J** to instantly undo the last sort and restore your inventory

### Loose Items
- Optionally sort loose items from your inventory into shulker boxes
- Mark items with `[KEEP]` in their name to exclude them from sorting

### Auto-Labeling
- Boxes are renamed by category (e.g. "Ores #1", "Blocks #2")
- Labels are translatable (English, German)
- Boxes with `[LOCKED]` in their name are excluded from sorting

### Tooltip Preview
- Hover over shulker boxes to see their contents
- Configurable max lines

### Feedback
- Chat notifications with box and item count
- Sound effect on successful sort
- Animated HUD overlay during sorting

## Installation

### Requirements
- **Fabric:** [Fabric Loader](https://fabricmc.net/), [Fabric API](https://modrinth.com/mod/fabric-api) and [Architectury API](https://modrinth.com/mod/architectury-api)
- **NeoForge:** [NeoForge](https://neoforged.net/) and [Architectury API](https://modrinth.com/mod/architectury-api)

### Optional
- [YACL](https://modrinth.com/mod/yacl) (for in-game configuration screen)
- [Mod Menu](https://modrinth.com/mod/modmenu) (Fabric only — for accessing config via mod list; on NeoForge the config button in the mod list works out of the box)

### Steps
1. Install your mod loader and the requirements listed above
2. Download the latest `shulkersorter-fabric-1.2.0+mc26.2.jar` or `shulkersorter-neoforge-1.2.0+mc26.2.jar` from [Releases](../../releases)
3. Place the JAR in your `.minecraft/mods/` folder
4. Launch Minecraft

## Configuration

Configuration is stored in `.minecraft/config/shulkersorter.toml` and can be edited in-game via Mod Menu + YACL.

| Setting | Default | Description |
|---------|---------|-------------|
| Auto-Label | On | Rename boxes by category after sorting |
| Overflow Mode | Fill | How overflow items are distributed (Fill / Dominant) |
| Include Loose Items | Off | Also sort loose inventory items into boxes |
| Skip Empty Boxes | Off | Exclude empty shulker boxes from sorting |
| Locked Tag | `[LOCKED]` | Boxes with this text in their name are skipped |
| Loose Item Ignore Tag | `[KEEP]` | Loose items with this text in their name are skipped |
| Tooltip | On | Show box contents on hover |
| Tooltip Max Lines | 5 | Maximum item lines in tooltip (1-27) |
| Chat Notifications | On | Chat messages on sort completion/failure |
| Sound Effects | On | Sound on successful sort |
| HUD Overlay | On | Animated overlay during sorting |

### Categories

The mod includes 11 default categories. Each can be reordered, enabled/disabled, and customized with item ID patterns via the in-game config screen or TOML config.

Pattern types:
- **Suffix patterns:** `_planks` matches `oak_planks`, `birch_planks`
- **Prefix patterns:** `raw_` matches `raw_iron`, `raw_gold`
- **Exact/substring:** `diamond` matches `diamond` and `diamond_block`

## Keybinds

| Key | Action |
|-----|--------|
| **J** | Sort all shulker boxes in inventory |
| **Shift+J** | Undo last sort |

## Building from Source

```bash
git clone https://github.com/DennisTheGamer/ShulkerSorter.git
cd ShulkerSorter
./gradlew build
```

The built JARs will be in `fabric/build/libs/` and `neoforge/build/libs/`.

## License

This project is licensed under the [MIT License](LICENSE).
