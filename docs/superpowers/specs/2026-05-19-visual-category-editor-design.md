# Visual Category Editor — Design Spec

**Date:** 2026-05-19  
**Branch:** mc26.1  
**Status:** Approved

---

## Overview

Replace the text-only pattern editing in the YACL Categories tab with a dedicated visual editor screen. Users can see which actual Minecraft items belong to each category and add new items by clicking — without needing to know item ID patterns. The existing wildcard pattern system is preserved in full; the editor is an additional interface on top of it.

---

## Goals

- Show category contents as real rendered Minecraft item icons
- Allow adding individual items to a category by clicking them in a searchable grid
- Keep the wildcard/pattern system (`_planks`, `raw_`, etc.) intact and editable
- Remain backward-compatible with existing TOML configs
- Launch from within the existing YACL Categories tab (YACL is not replaced)

---

## Entry Point

The YACL Categories tab (`ConfigScreen.java`) gains a single button at the top of the tab:

```
[ 🎨 Visual Editor öffnen → ]
```

Clicking it calls `Minecraft.getInstance().setScreen(new CategoryEditorScreen(parent))`.

---

## Screen: `CategoryEditorScreen`

**File:** `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java`  
**Extends:** `net.minecraft.client.gui.screens.Screen`

### Layout

```
┌─────────────────────────────────────────────────────────┐
│              ✦ CATEGORY VISUAL EDITOR ✦                 │
├──────────┬──────────┬──────────┬──────────┬─────────────┤
│ Redstone │  Nature  │  Tools   │ Transport│  Food  +6…  │  ← tab bar
├──────────────────────────┬──────────────────────────────┤
│  LEFT PANE               │  RIGHT PANE                  │
│  "⚡ Redstone — 18 Items"│  [🔍 Suche: repeater...]     │
│                          │                              │
│  [item grid: in-cat]     │  [item grid: all items]      │
│  (click to remove)       │  (click to add)              │
│                          │                              │
│  Wildcards:              │  ↕ scrollable ~1200 items    │
│  [repeater ✕][_det ✕]   │                              │
│  [+ Wildcard hinzufügen] │                              │
├──────────────────────────┴──────────────────────────────┤
│  Changes saved to TOML on close    [✕ Cancel] [✔ Save]  │
└─────────────────────────────────────────────────────────┘
```

---

## Component Details

### Tab Bar

- One tab per entry in `config.categoryOrder`
- Active tab highlighted; inactive tabs dimmed
- Disabled categories (in `config.disabledCategories`) shown with a grey dot and `(off)` label
- If tabs overflow horizontally: show a `… +N` overflow indicator (no scrolling tabs — just truncate)
- Clicking a tab saves pending changes to the current category before switching

### Left Pane — "Items in this category"

- Title shows category name + count of matched items
- Item grid: all items from `BuiltInRegistries.ITEM` whose ID matches at least one pattern in the current category — determined by reusing `ItemCategorizer` matching logic
- Items rendered with `GuiGraphics.renderItem` (real textures, 16×16 slots with 2px border)
- Hover tooltip shows item ID (e.g. `minecraft:repeater`)
- **Click an item** → removes its exact ID from the patterns list (only works if it was added as exact ID; wildcard-only matches show a visual indicator that they cannot be individually removed — removing the wildcard chip handles them)
- Scrollable via mouse wheel; scroll offset clamped to content height

### Wildcard Chips

- Rendered below the item grid as pill-shaped text chips
- Each chip shows the raw pattern string (e.g. `_detector`, `raw_`)
- **Click ✕** on a chip → removes that pattern string from the list; item grid updates immediately
- **"+ Wildcard hinzufügen"** chip → opens a small inline text input; pressing Enter or clicking "✔" adds the typed string as a new pattern

### Right Pane — "All Items"

- Search bar at top: filters by translated item name (case-insensitive substring match)
- Item grid below: all Minecraft items matching the search, rendered identically to the left pane
- Items already in the current category are shown with a green tint (so the user can see what's already assigned)
- **Click an item** → adds `minecraft:<item_id>` as a new exact pattern; item immediately appears in the left pane
- Scrollable via mouse wheel

---

## Data Flow

```
CategoryEditorScreen opens
  └─ reads ShulkerSorterConfig.getInstance()
  └─ holds a local working copy of each CategoryDefinition's patterns list

User edits (add/remove items, add/remove wildcards)
  └─ mutate the local working copy only

"Save & Close" clicked
  └─ write local copies back to CategoryDefinition.setPatterns(...)
  └─ config.save()
  └─ close screen → return to YACL parent

"Cancel" clicked
  └─ discard local copy, close screen → return to YACL parent
```

---

## Changes to Existing Files

| File | Change |
|---|---|
| `ConfigScreen.java` | Add "Visual Editor öffnen" button at top of Categories tab |
| `CategoryDefinition.java` | No changes |
| `ShulkerSorterConfig.java` | No changes |
| `ItemCategorizer.java` | Extract static helper `matchesPatterns(String itemId, List<String> patterns): boolean` for reuse in the editor |

---

## New Files

| File | Purpose |
|---|---|
| `CategoryEditorScreen.java` | Full custom screen |
| `CategoryEditorScreen$ItemScrollPane.java` (inner class) | Scrollable item grid widget used for both panes |

---

## Out of Scope

- Drag-to-reorder categories (category order stays in YACL)
- Creating or deleting custom categories (stays in YACL)
- Mod-added items from other mods (only `BuiltInRegistries.ITEM` scope — vanilla items only for the initial version)
- Enabling/disabling categories (stays in YACL)
