# 📦 ShulkerSort

**Sort, merge and label your shulker boxes with a single keypress.**

ShulkerSort is a lightweight, client-side Fabric mod that automatically organizes all items across your shulker boxes by category. Press one key — your ores go into one box, your building blocks into another, your tools into a third. Every box gets a clean label. No more shulker box chaos.

---

## ✨ Features

### 🗂️ One-Key Sorting
- **Press J** to sort all shulker boxes in your inventory instantly
- **Smart categorization** — Items are grouped into categories: Blocks, Tools, Food, Ores, Brewing, Misc
- **Stack merging** — Partial stacks of the same item are merged before distributing
- **Category separation** — Each category starts in a fresh shulker box

### 🏷️ Auto-Labeling
- **Automatic renaming** — Sorted boxes get named by their category (e.g. "Ores #1", "Blocks #2")
- **Translatable labels** — Box names follow your game language (English, German)
- **Lock protection** — Add `[LOCKED]` to a box name to exclude it from sorting

### 🖥️ Tooltip Preview
- **Hover preview** — See shulker box contents without opening them
- **Configurable** — Adjust max lines shown in the tooltip

### 🔔 Feedback
- **Chat notifications** — See how many boxes were sorted
- **Sound effects** — Audio feedback on successful sorting
- **HUD overlay** — Animated "Sorting..." indicator during the process

---

## 🎮 Controls

| Key | Action |
|-----|--------|
| **J** | Sort all shulker boxes in inventory |

---

## ⚙️ Customization

Configure the mod via [Mod Menu](https://modrinth.com/mod/modmenu) + [Cloth Config](https://modrinth.com/mod/cloth-config), or by editing `.minecraft/config/shulkersort.toml`:

- Auto-labeling on/off
- Locked tag (default: `[LOCKED]`)
- Tooltip on/off and max lines
- Chat notifications, sound effects, HUD overlay
- Custom category order and patterns
- Add your own categories with custom item patterns
- Debug logging

---

## 📋 Requirements

- **Minecraft:** 1.21 – 1.21.8
- **Fabric Loader:** 0.18.3 or higher
- **Fabric API:** Required
- **Java:** 21 or higher
- **Mod Menu + Cloth Config:** Optional (for in-game config screen)

---

## 🚀 How It Works

1. Fill some shulker boxes with mixed items
2. Keep the shulker boxes in your inventory
3. Press **J** — all items are categorized, merged, and distributed
4. Each box gets labeled by its category (e.g. "Tools #1", "Ores #2")
5. Done! Your shulker boxes are organized.

Boxes with `[LOCKED]` in their name are skipped — perfect for keeping a box exactly how you want it.

---

## 💡 Perfect For

- **Hoarders** — Finally organize that inventory full of random shulker boxes
- **Builders** — Keep building blocks neatly separated from tools and ores
- **Miners** — Auto-sort your mining haul into clean categories
- **Base Organizers** — Label and sort boxes before storing them in your base

---

## ❓ FAQ

**Does this mod work on servers?**
Yes! It's fully client-side and works on any server without server-side installation.

**Does it affect performance?**
No. ShulkerSort only runs when you press the sort key.

**Can I customize categories?**
Yes! Edit `shulkersort.toml` to add, remove, or reorder categories with custom item patterns.

**What if I don't have enough shulker boxes?**
Sorting is cancelled safely — no items are lost.

---

## 🌍 Languages

Available in **English** and **German**.

## 🔗 Links

- **Issues & Bugs:** [GitHub Issues](https://github.com/DennisTheGamer/ShulkerSort/issues)
- **Source Code:** [GitHub Repository](https://github.com/DennisTheGamer/ShulkerSort)

## 📜 License

This mod is open-source and licensed under the **MIT License**.
