# Changelog

## [1.0.0]

### Features
- **One-key sorting** — Press J to sort all shulker boxes in your inventory
- **11 item categories** — Redstone, Transport, Nature, Mob Loot, Decoration, Blocks, Tools, Food, Ores, Brewing, Misc
- **Overflow modes** — FILL (pack items across boxes) or DOMINANT (one category per box)
- **Undo** — Press Shift+J to undo the last sort
- **Loose items** — Optionally sort loose inventory items into shulker boxes
- **Skip empty boxes** — Option to exclude empty shulker boxes from sorting
- **Disable categories** — Enable/disable individual categories
- **Affinity-based distribution** — Boxes keep their dominant category when re-sorting
- **Two-pass pattern matching** — Specific patterns (prefix/suffix) checked before substring matches
- **Auto-labeling** — Sorted boxes get named by category (e.g. "Ores #1", "Blocks #2")
- **Lock protection** — Add `[LOCKED]` to a box name to exclude it from sorting
- **Loose item ignore tag** — Mark items with `[KEEP]` to exclude them from sorting
- **Shulker box tooltip preview** — Hover to see contents, configurable max lines
- **Chat notifications** — Box and item count on sort completion
- **Sound effects** — Audio feedback on successful sort
- **HUD overlay** — Animated "Sorting..." indicator during sorting
- **Categories tab** — Full in-game editor for category order, enable/disable, and patterns
- **YACL config screen** — All settings configurable via Mod Menu + YACL
- **TOML config** — Manual editing via `.minecraft/config/shulkersorter.toml`
- **Singleplayer** — Sorting runs on the integrated server thread
- **Multiplayer survival** — Server-side sorting via custom networking packets (mod required on server)
- **Multiplayer creative** — Sort works on servers in creative mode (no server mod needed)
- **Graceful fallback** — Shows clear message when server doesn't have the mod installed
- **English and German** language support
