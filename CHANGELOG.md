# Changelog

## [1.2.1] - 2026-07-21

### Changed
- Housekeeping only: internal working files are no longer tracked in the repository.

## [1.2.0] - 2026-07-14

### Changed
- Unified the mod version across all loaders (Fabric & NeoForge) and every supported Minecraft version, so all builds now ship as `1.2.0`
- Standardized jar file naming to `shulkersorter-<loader>-<version>+mc<range>.jar` (e.g. `shulkersorter-fabric-1.2.0+mc1.21.9-1.21.11.jar`, `shulkersorter-neoforge-1.2.0+mc1.21.9-1.21.11.jar`)
- Corrected author and contact metadata (Modrinth page and GitHub links)

## [1.1.2]

### Fixed
- **NeoForge jar crashed on Minecraft 1.21.9/1.21.10** (`ClassNotFoundException: net.minecraft.resources.Identifier`): NeoForge jars run directly on the Mojang class names of the runtime version, and Mojang renamed `ResourceLocation` to `Identifier` in 1.21.11. All direct references were removed from version-critical paths: the sort keybind now uses the vanilla "Inventory" category, the network payload types are created through the loader-specific `Platform` service (reflective on NeoForge), and item ids are read via `Item.toString()` instead of `Registry.getKey`. A full symbol diff of the shipped jar against the 1.21.9/1.21.10/1.21.11 mappings shows no remaining version-unstable references

## [1.1.1]

### Fixed
- **Lowered the required Architectury API to 18.0.3** on both loaders — 19.x only runs on Minecraft 1.21.11, so requiring it made the mod uninstallable on 1.21.9/1.21.10 (use Architectury 18.x there; 19.x on 1.21.11)

## [1.1.0]

### Added
- **NeoForge support** — the mod now ships as two jars built from one codebase: `shulkersorter-fabric-<version>.jar` (Fabric) and `shulkersorter-neoforge-<version>.jar` (NeoForge)
- **Architectury API** is now a required dependency on both loaders (used for events and networking)
- On NeoForge the config screen is reachable via the mod list's config button (requires YACL); on Fabric it stays available via Mod Menu

### Changed
- Restructured the project into `common/` (shared code), `fabric/` and `neoforge/` subprojects (Architectury template with `dev.architectury.loom-remap`; sources migrated from Yarn to Mojang mappings)
- Build toolchain pinned: Architectury Loom-Remap 1.14.476, Architectury Plugin 3.5.168, Architectury API 19.0.1, NeoForge 21.11.42 (Loom was `1.14-SNAPSHOT` before)
- ModMenu bumped to 17.0.0 (was 17.0.0-alpha.1)

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
