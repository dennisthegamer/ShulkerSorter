# Changelog

## [1.2.1] - 2026-07-21

### Changed
- Housekeeping only: internal working files are no longer tracked in the repository.

## [1.2.0] - 2026-07-14

### Changed
- Unified the mod version across all loaders (Fabric & NeoForge) and every supported Minecraft version, so all builds now ship as `1.2.0`
- Standardized jar file naming to `shulkersorter-<loader>-<version>+mc<range>.jar` (e.g. `shulkersorter-fabric-1.2.0+mc26.1-26.1.2.jar`, `shulkersorter-neoforge-1.2.0+mc26.1-26.1.2.jar`)
- Corrected author and contact metadata (Modrinth page and GitHub links)

## [1.1.1]

### Fixed
- **Lowered the required Architectury API to 20.0.4** on both loaders — 20.0.5 and newer only run on Minecraft 26.1.2, so requiring 20.0.7 made the mod uninstallable on 26.1/26.1.1 (Fabric)
- **The NeoForge jar now requires Minecraft 26.1.2** — no Architectury 20.x release can load on NeoForge below 26.1.2 (it needs NeoForge's `BreakBlockEvent`, added in 26.1.2), so 26.1/26.1.1 support is Fabric-only

## [1.1.0]

### Added
- **NeoForge support** — the mod now ships as two jars built from one codebase: `shulkersorter-fabric-<version>.jar` (Fabric) and `shulkersorter-neoforge-<version>.jar` (NeoForge)
- **Architectury API** is now a required dependency on both loaders (used for events and networking)
- On NeoForge the config screen is reachable via the mod list's config button (requires YACL); on Fabric it stays available via Mod Menu

### Changed
- Restructured the project into `common/` (shared code), `fabric/` and `neoforge/` subprojects
- Updated YACL to 3.9.5+26.1 (3.9.1 is no longer available on the maven)

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
