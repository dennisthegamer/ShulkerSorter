# Changelog

## [1.1.0]

### Fixed
- Tooltip "more items" counter now shows total item count instead of type count
- Removed unused `keybind` config field (keybind is managed via Minecraft's Controls screen)

### Improved
- Sort success message now includes item count: "3 boxes sorted, 128 items distributed"

### Added
- **Skip Empty Boxes** option: empty shulker boxes are ignored during sorting when enabled
- **Undo** (Shift+J): restores inventory to pre-sort state; works in singleplayer and multiplayer creative
- **Categories tab** in config screen: reorder categories, enable/disable individual categories, edit item ID patterns per category

## [1.0.0]

- Initial release
- One-key sorting of all shulker boxes in inventory
- Smart item categorization: Blocks, Tools, Food, Ores, Brewing, Misc
- Automatic stack merging before distribution
- Auto-labeling with translatable category names
- Lock protection via `[LOCKED]` tag
- Shulker box tooltip preview
- Chat notifications, sound effects, and HUD overlay
- Fully configurable via TOML config and YACL config screen
- English and German language support
