# ShulkerSort

A client-side Fabric mod that automatically sorts, merges and labels shulker boxes in your inventory by item category.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21–1.21.8-green)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Version](https://img.shields.io/badge/Version-1.0.0-orange)

## Features

### Sorting Engine
- Press **S** to sort all shulker boxes in your inventory
- Items are categorized into groups: Blocks, Tools, Food, Ores, Brewing, Misc
- Partial stacks of the same item are merged automatically
- Each category starts in a new shulker box
- Validates capacity before sorting — no items are ever lost

### Auto-Labeling
- Boxes are renamed by category (e.g. "Ores #1", "Blocks #2")
- Labels are translatable (English, German)
- Boxes with `[LOCKED]` in their name are excluded from sorting

### Tooltip Preview
- Hover over shulker boxes to see their contents
- Configurable max lines

### Feedback
- Chat notifications on sort completion or errors
- Sound effect on successful sort
- Animated HUD overlay during sorting

## Installation

### Requirements
- Minecraft 1.21 - 1.21.8
- [Fabric Loader](https://fabricmc.net/) >= 0.18.3
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 21+

### Optional
- [Cloth Config](https://modrinth.com/mod/cloth-config) >= 15.0.0 (for in-game configuration screen)
- [Mod Menu](https://modrinth.com/mod/modmenu) (for accessing config via mod list)

### Steps
1. Install Fabric Loader and Fabric API
2. Download the latest `shulkersort-x.x.x.jar` from [Releases](../../releases)
3. Place the JAR in your `.minecraft/mods/` folder
4. Launch Minecraft

## Configuration

Configuration is stored in `.minecraft/config/shulkersort.toml` and can be edited in-game via Mod Menu + Cloth Config.

| Setting | Default | Description |
|---------|---------|-------------|
| Auto-Label | On | Rename boxes by category after sorting |
| Locked Tag | `[LOCKED]` | Boxes with this text in their name are skipped |
| Tooltip | On | Show box contents on hover |
| Tooltip Max Lines | 5 | Maximum item lines in tooltip (1-27) |
| Chat Notifications | On | Chat messages on sort completion/failure |
| Sound Effects | On | Sound on successful sort |
| HUD Overlay | On | Animated overlay during sorting |
| Debug Logging | Off | Detailed logging for troubleshooting |
| Category Order | blocks, tools, food, ores, brewing, misc | Order in which categories are sorted |

### Custom Categories

Categories can be customized in the TOML config. Each category has a label and a list of item ID patterns:

- **Suffix patterns:** `_planks` matches `oak_planks`, `birch_planks`
- **Prefix patterns:** `raw_` matches `raw_iron`, `raw_gold`
- **Exact/contains:** `diamond` matches `diamond` and `diamond_block`

## Keybinds

| Key | Action |
|-----|--------|
| **S** | Sort all shulker boxes in inventory |

## Building from Source

```bash
git clone https://github.com/DennisTheGamer/ShulkerSort.git
cd ShulkerSort
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

This project is licensed under the [MIT License](LICENSE).
