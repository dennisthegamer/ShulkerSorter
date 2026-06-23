# Changelog

## [1.1.0]

### Added
- **Overflow Modes** — Choose between FILL (pack items across boxes) and DOMINANT (one category per box)
- **Undo** — Press Shift+J to undo the last sort
- **Loose Items** — Optionally sort loose inventory items into shulker boxes
- **Skip Empty Boxes** — Option to exclude empty shulker boxes from sorting
- **Disable Categories** — Enable/disable individual categories in the config
- **Categories Tab** — Full in-game editor for category order, enable/disable, and patterns
- **Affinity-based Distribution** — Boxes keep their dominant category when re-sorting
- **Loose Item Ignore Tag** — Mark items with `[KEEP]` to exclude them from sorting
- **5 New Categories** — Redstone, Transport, Nature, Mob Loot, Decoration
- **Two-pass Pattern Matching** — Specific patterns (prefix/suffix) are checked before substring matches
- **Multiplayer Creative Support** — Sort works on servers in creative mode
- **Singleplayer Server-side Sorting** — Sorting runs on the integrated server thread for reliability
- **New Translation Keys** — All new features fully translated (English, German)

### Changed
- Config screen now uses **YACL** instead of Cloth Config
- Package renamed from `com.shulkersort` to `de.dennisthegamer.shulkersort`
- Success message now shows item count alongside box count

### Fixed
- **Backfill bug** — Items from unrelated categories no longer get placed into affinity boxes (e.g. Chicken in Redstone box). Overflow items now go to MISC boxes instead.

## [1.0.0]

### Added
- Initial release
- One-key sorting of all shulker boxes in inventory (press J)
- Smart item categorization: Blocks, Tools, Food, Ores, Brewing, Misc
- Automatic stack merging before distribution
- Auto-labeling with translatable category names
- Lock protection via `[LOCKED]` tag
- Shulker box tooltip preview
- Chat notifications, sound effects, and HUD overlay
- Fully configurable via TOML config and config screen
- English and German language support
