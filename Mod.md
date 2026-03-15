Erstelle eine Minecraft Fabric Mod namens „ShulkerSort" für Minecraft

🎯 Ziel
Der Spieler soll alle Shulker Boxes im Inventar per Keybind automatisch analysieren, nach Inhaltskategorie sortieren und zusammenführen können — ohne zusätzliche Blöcke oder GUIs.

📦 Funktion 1: Inventar-Scan

Beim Drücken des Keybinds (Standard: S) scannt die Mod alle Shulker Boxes im Spieler-Inventar (9 Hotbar-Slots + 27 Inventar-Slots).
Jede Shulker Box wird virtuell geöffnet und ihr Inhalt ausgelesen (NBT-Daten).
Shulker Boxes die leer sind werden ignoriert.
Shulker Boxes die gesperrt sind (Config-Option: „locked"-Tag per Rename z.B. „[LOCKED] Kiste") werden übersprungen und nicht verändert.


🗂️ Funktion 2: Kategorisierung
Items werden anhand ihrer Registry-ID einer Kategorie zugewiesen. Vordefinierte Kategorien:

🪨 Blöcke & Baumaterialien — alle _log, _planks, _stone, _brick, _slab, _stairs, sand, gravel, dirt, glass etc.
⚔️ Werkzeuge, Waffen & Rüstung — alle _sword, _pickaxe, _axe, _shovel, _hoe, _helmet, _chestplate, _leggings, _boots, bow, crossbow, shield, trident
🌾 Nahrung & Pflanzen — alle Items mit food-Komponente, Seeds, Saplings, Flowers
💎 Erze & Rohstoffe — alle _ore, _ingot, _nugget, raw_, _gem, coal, quartz, ancient_debris, netherite
🧪 Brauerei & Tränke — potion, splash_potion, lingering_potion, brewing_stand, alle _dye, glass_bottle
🗃️ Sonstiges — alles was keiner anderen Kategorie zugewiesen werden kann

Kategorien sind vollständig per Config überschreibbar (eigene Item-Listen als Whitelist pro Kategorie, Reihenfolge der Kategorien konfigurierbar).

🔀 Funktion 3: Merge-Logik

Alle Items derselben Kategorie werden aus ihren Shulker Boxes herausgenommen und neu verteilt.
Stacks werden zuerst aufgefüllt (bis zur maximalen Stackgröße des Items) bevor ein neuer Stack begonnen wird.
Items werden in möglichst wenige Shulker Boxes gepackt (Bin-Packing-Logik).
Wenn nach dem Sortieren eine Shulker Box leer ist, bleibt sie leer im Inventar (wird nicht entfernt).
Falls nicht genug Shulker Boxes für alle Items vorhanden sind, wird der Spieler per Chat-Nachricht in roter Farbe gewarnt: „ShulkerSort: Nicht genug Shulker Boxes für alle Items! Sortierung abgebrochen."
Die Sortierung passiert atomar — entweder komplett oder gar nicht (kein halber Zustand).


🏷️ Funktion 4: Auto-Labeling

Nach dem Sortieren wird jede Shulker Box automatisch per Custom Name beschriftet, z.B. „Blöcke #1", „Blöcke #2", „Erze #1".
Die Nummerierung erfolgt fortlaufend pro Kategorie.
Bereits vorhandene Custom Names werden überschrieben, außer die Box hat den „[LOCKED]"-Tag.
Auto-Labeling kann per Config deaktiviert werden.
Label-Präfixe pro Kategorie sind per Config anpassbar (z.B. statt „Blöcke" → „Blocks").


🔍 Funktion 5: Vorschau-Tooltip

Beim Hovern über eine Shulker Box im Inventar wird ein Tooltip eingeblendet, der alle enthaltenen Items mit Icon, Name und Anzahl auflistet (analog zu How Does It Stack oder Shulker Box Tooltip).
Der Tooltip zeigt maximal 5 Zeilen; bei mehr Items erscheint „... und X weitere Items".
Der Tooltip funktioniert überall: Inventar, Truhen-GUI, Hotbar-Hover (mit gedrücktem Shift).
Tooltip kann per Config deaktiviert werden.


⚙️ Config-Datei (shulkersort.toml)
OptionTypStandardBeschreibungkeybindString"s"Keybind zum Auslösen der Sortierungauto_labelBooleantrueShulker Boxes nach Sortierung beschriftentooltip_enabledBooleantrueVorschau-Tooltip aktivierentooltip_max_linesInteger5Max. Zeilen im Tooltiplocked_tagString"[LOCKED]"Prefix um Boxen vom Sortieren auszuschließencategoriesTablesiehe obenEigene Item-Listen pro Kategoriecategory_orderArray["blocks","tools","food","ores","brewing","misc"]Reihenfolge der Kategorien

🔊 Feedback & UX

Beim erfolgreichen Sortieren: kurzes „Pling"-Sound-Effekt + grüne Chat-Nachricht „ShulkerSort: X Boxen sortiert."
Bei Fehler (zu wenig Boxen, locked Boxen übersprungen): rote Chat-Nachricht mit Details.
Während der Sortierung: kurze Ladeanimation im HUD (spinning Icon, max. 1 Sekunde) damit der Spieler weiß dass etwas passiert.


Ziel-Plattform: Fabric für Minecraft Version ab 1.21 bis 1.21.11

Für Referenzen wie du die Mod am besten bauen kannst ist das existierende Projekt beziehungsweise die existierende Mod an folgendem Pfad zu finden. C:\Users\mager\Downloads\inventory_shulker-template-1.21