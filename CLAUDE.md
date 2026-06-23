# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the mod JAR (output in build/libs/)
./gradlew build

# Run the Minecraft client with the mod loaded (dev environment)
./gradlew runClient

# Clean build artifacts
./gradlew clean
```

No test suite exists; verification is done by running the client.

## Project Overview

ShulkerSort is a **client-side Fabric mod** for Minecraft 26.1+ that automatically sorts, merges, and labels shulker boxes in the player's inventory when **J** is pressed.

Key dependencies (versions in `gradle.properties`):
- Minecraft 26.1 — **unobfuscated**, so no mappings are used in `build.gradle`
- Fabric Loader + Fabric API
- **YACL** (`yet-another-config-lib`) — config screen, `compileOnly`/`localRuntime` only (users install separately)
- **ModMenu** — dev-only, exposes the config screen entry point

The mod uses `loom.splitEnvironmentSourceSets()`, so source sets are split:
- `src/main/` — server-safe code (config, sort engine, utilities)
- `src/client/` — client-only code (keybind, HUD, tooltip, config screen)

## Architecture

### Entry Points
- `ShulkerSort` (main) — `ModInitializer`, only logs initialization
- `ShulkerSortClient` (client) — `ClientModInitializer`, registers config, keybind, tooltip renderer, HUD overlay

### Sorting Pipeline (`ShulkerSortEngine.sort`)
The sort runs in 7 sequential phases:
1. **SCAN** — find shulker boxes in inventory, skip locked ones
2. **CATEGORIZE** — classify each item via `ItemCategorizer`, collect loose items based on `overflowMode`
3. **MERGE** — merge partial stacks of the same item type
4. **VALIDATE** — hard-fail if items won't fit in available box slots
5. **DISTRIBUTE** — assign items to boxes using affinity (boxes keep their dominant category) then fill remaining boxes via `FILL` or `DOMINANT` mode
6. **LABEL** — generate translatable names like "Ores #1" per category counter
7. **APPLY** — write new contents/names back to `ItemStack`s, clear sorted loose item slots

### Item Categorization (`ItemCategorizer`)
Two-pass matching against patterns in `ShulkerSortConfig.categoryOrder`:
- **Pass 1 (specific):** prefix patterns (`raw_`), suffix patterns (`_planks`), exact matches; food items short-circuit to the `food` category via the `DataComponents.FOOD` component
- **Pass 2 (contains):** substring matches for non-specific patterns
- Falls through to `misc` if nothing matches

### Configuration (`ShulkerSortConfig`)
- Singleton loaded from `.minecraft/config/shulkersort.toml` via a hand-written `TomlParser`
- `categories` map holds `CategoryDefinition` (label key + pattern list); built-in categories are always initialized in code, TOML can override patterns but not translation keys
- `overflowMode` (`FILL` / `DOMINANT`) controls how items beyond affinity-matched boxes are distributed:
  - `FILL` — packs items across free boxes in `categoryOrder` priority
  - `DOMINANT` — each category claims its own box; spillover is packed into remaining slots

### Config Screen (`ConfigScreen`)
Built with YACL3. Guarded at compile-time with `compileOnly`; `ModMenuIntegration` supplies the screen factory only when YACL is present at runtime.

### Client-side Components
| Class | Purpose |
|---|---|
| `SortKeybindHandler` | Registers the **J** keybind; triggers `ShulkerSortEngine.sort` and dispatches result notifications |
| `ShulkerTooltipRenderer` | Injects shulker box content preview into item tooltips |
| `SortingHudOverlay` | Animated overlay displayed during sorting |
| `NotificationHelper` | Sends chat messages and plays sound effects based on `SortResult` |

### Locked / Ignored Items
- Shulker boxes with `config.lockedTag` (default `[LOCKED]`) in their custom name are skipped entirely
- Loose items with `config.looseItemIgnoreTag` (default `[KEEP]`) in their custom name are skipped when `includeLooseItems` is enabled
