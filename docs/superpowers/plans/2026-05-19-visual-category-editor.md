# Visual Category Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a custom Minecraft screen accessible from the YACL Categories tab that lets users visually browse and edit which items belong to each sort category using rendered item icons, while keeping the existing wildcard-pattern system intact.

**Architecture:** A `CategoryEditorScreen extends Screen` holds local working copies of each category's pattern lists. It renders a tab bar (one tab per category), a left pane showing items that currently match the category's patterns, and a right pane showing all Minecraft items with a search box. Clicking a right-pane item adds its exact ID as a pattern; clicking a left-pane item removes it (if it was added as an exact ID). Wildcard chips below the left grid allow viewing and removing patterns. Save writes the working copies back to `CategoryDefinition` and calls `config.save()`, then re-opens a fresh `ConfigScreen` to avoid stale YACL pending values.

**Tech Stack:** Fabric mod (MC 26.1 unobfuscated), YACL3 for the entry-point button, vanilla `Screen` / `GuiGraphics` / `Button` / `EditBox` for the editor itself.

---

## File Map

| Status | File | Change |
|---|---|---|
| Modify | `src/main/java/com/shulkersorter/sort/ItemCategorizer.java` | Add public `matchesAnyPattern` helper |
| Modify | `src/main/resources/assets/shulkersorter/lang/en_us.json` | Add 7 translation keys |
| Modify | `src/main/resources/assets/shulkersorter/lang/de_de.json` | Add 7 translation keys (German) |
| Modify | `src/client/java/com/shulkersorter/config/ConfigScreen.java` | Add ButtonOption in categories tab |
| Create | `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java` | Full custom editor screen |

---

## Task 1: Extract `matchesAnyPattern` in `ItemCategorizer`

**Files:**
- Modify: `src/main/java/com/shulkersorter/sort/ItemCategorizer.java`

- [ ] **Step 1: Add the public helper method**

  In `ItemCategorizer.java`, add after the existing `categorize` method (before `isSpecificPattern`):

  ```java
  /**
   * Returns true if itemId matches any pattern in the list using both passes
   * (specific prefix/suffix/exact, then substring). Reuses the same logic as categorize().
   */
  public static boolean matchesAnyPattern(String itemId, List<String> patterns) {
      // Pass 1: specific matches (prefix/suffix/exact)
      for (String pattern : patterns) {
          if (matchesSpecific(itemId, pattern)) return true;
      }
      // Pass 2: substring matches
      for (String pattern : patterns) {
          if (!isSpecificPattern(pattern) && itemId.contains(pattern)) return true;
      }
      return false;
  }
  ```

- [ ] **Step 2: Verify compilation**

  ```bash
  ./gradlew build
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

  ```bash
  git add src/main/java/com/shulkersorter/sort/ItemCategorizer.java
  git commit -m "refactor: expose matchesAnyPattern helper in ItemCategorizer"
  ```

---

## Task 2: Add translation keys

**Files:**
- Modify: `src/main/resources/assets/shulkersorter/lang/en_us.json`
- Modify: `src/main/resources/assets/shulkersorter/lang/de_de.json`

- [ ] **Step 1: Add keys to `en_us.json`**

  Append inside the JSON object (before the closing `}`), after the last `category.patterns.tooltip` entry:

  ```json
  ,
  "config.shulkersorter.visual_editor.open": "Visual Editor",
  "config.shulkersorter.visual_editor.title": "Category Visual Editor",
  "config.shulkersorter.visual_editor.save": "Save & Close",
  "config.shulkersorter.visual_editor.cancel": "Cancel",
  "config.shulkersorter.visual_editor.add_wildcard": "+ Add Wildcard",
  "config.shulkersorter.visual_editor.wildcard_hint": "pattern (e.g. _planks, raw_)",
  "config.shulkersorter.visual_editor.search_hint": "Search items..."
  ```

- [ ] **Step 2: Add keys to `de_de.json`**

  Same position in the German file:

  ```json
  ,
  "config.shulkersorter.visual_editor.open": "Visueller Editor",
  "config.shulkersorter.visual_editor.title": "Kategorie-Editor",
  "config.shulkersorter.visual_editor.save": "Speichern & Schliessen",
  "config.shulkersorter.visual_editor.cancel": "Abbrechen",
  "config.shulkersorter.visual_editor.add_wildcard": "+ Wildcard hinzufuegen",
  "config.shulkersorter.visual_editor.wildcard_hint": "Muster (z.B. _planks, raw_)",
  "config.shulkersorter.visual_editor.search_hint": "Items suchen..."
  ```

- [ ] **Step 3: Build**

  ```bash
  ./gradlew build
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

  ```bash
  git add src/main/resources/assets/shulkersorter/lang/
  git commit -m "i18n: add visual editor translation keys"
  ```

---

## Task 3: Add "Visual Editor" button in ConfigScreen

**Files:**
- Modify: `src/client/java/com/shulkersorter/config/ConfigScreen.java`

- [ ] **Step 1: Add import**

  At the top of `ConfigScreen.java`, add:

  ```java
  import dev.isxander.yacl3.api.ButtonOption;
  import net.minecraft.client.Minecraft;
  ```

- [ ] **Step 2: Add ButtonOption at top of categories tab**

  In `ConfigScreen.create(Screen parent)`, change the categories tab builder. The existing first line of the tab:

  ```java
  ConfigCategory.Builder categoriesTab = ConfigCategory.createBuilder()
          .name(Component.translatable("config.shulkersorter.category.categories"));
  ```

  Replace with:

  ```java
  ConfigCategory.Builder categoriesTab = ConfigCategory.createBuilder()
          .name(Component.translatable("config.shulkersorter.category.categories"))
          .option(ButtonOption.createBuilder()
                  .name(Component.translatable("config.shulkersorter.visual_editor.open"))
                  .description(OptionDescription.EMPTY)
                  .action((lib, opt) -> Minecraft.getInstance().setScreen(
                          new CategoryEditorScreen(Minecraft.getInstance().screen, parent)))
                  .build());
  ```

  Note: `parent` here is the `Screen parent` parameter of `ConfigScreen.create(Screen parent)` — this is the screen that opened YACL. It is captured in the lambda and passed to `CategoryEditorScreen` so that Save can re-open a fresh config screen.

- [ ] **Step 3: Build**

  ```bash
  ./gradlew build
  ```

  If `ButtonOption` is not found: check the YACL version in `gradle.properties`. For YACL3 < 3.1, use this alternative (creates a boolean option that opens the screen on any interaction):

  ```java
  // Alternative if ButtonOption unavailable:
  .option(Option.<Boolean>createBuilder()
          .name(Component.translatable("config.shulkersorter.visual_editor.open"))
          .binding(false, () -> false,
                  val -> Minecraft.getInstance().setScreen(
                          new CategoryEditorScreen(Minecraft.getInstance().screen, parent)))
          .controller(TickBoxControllerBuilder::create)
          .build())
  ```

- [ ] **Step 4: Commit**

  ```bash
  git add src/client/java/com/shulkersorter/config/ConfigScreen.java
  git commit -m "feat: add Visual Editor button to YACL categories tab"
  ```

---

## Task 4: Create `CategoryEditorScreen` skeleton

**Files:**
- Create: `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java`

This task creates the full class with all fields, `init()`, `render()` stub, `save`/`cancel`, and the three item-list helpers. Later tasks fill in the rendering methods.

- [ ] **Step 1: Create the file**

  ```java
  package com.shulkersorter.config;

  import com.shulkersorter.sort.ItemCategorizer;
  import net.minecraft.client.gui.GuiGraphics;
  import net.minecraft.client.gui.components.Button;
  import net.minecraft.client.gui.components.EditBox;
  import net.minecraft.client.gui.screens.Screen;
  import net.minecraft.core.registries.BuiltInRegistries;
  import net.minecraft.network.chat.Component;
  import net.minecraft.resources.Identifier;
  import net.minecraft.world.item.Item;
  import net.minecraft.world.item.ItemStack;

  import java.util.*;

  public class CategoryEditorScreen extends Screen {

      // Layout constants
      private static final int SLOT_SIZE   = 20;  // item slot incl. 2px padding each side
      private static final int TAB_H       = 20;
      private static final int BOTTOM_BAR_H = 28;
      private static final int PADDING     = 6;
      private static final int PANE_GAP    = 4;
      private static final int CHIP_AREA_H = 52;  // fixed height below item grid for chips

      // Colors (ARGB)
      private static final int C_BG           = 0xFF1a1a1a;
      private static final int C_PANE_BG      = 0xFF222222;
      private static final int C_SLOT         = 0xFF3a3a3a;
      private static final int C_SLOT_IN      = 0xFF1e3318;  // green tint — in category
      private static final int C_SLOT_HOVER   = 0x50FFFFFF;  // white overlay on hover
      private static final int C_TAB_ACTIVE   = 0xFF383838;
      private static final int C_TAB_INACTIVE = 0xFF252525;
      private static final int C_TAB_BORDER   = 0xFF666666;
      private static final int C_CHIP_BG      = 0xFF1c2e1c;
      private static final int C_CHIP_BORDER  = 0xFF3a6a3a;
      private static final int C_TEXT         = 0xFFCCCCCC;
      private static final int C_TEXT_DIM     = 0xFF888888;
      private static final int C_BOTTOM_LINE  = 0xFF444444;

      // --- State ---
      private final Screen yaclScreen;   // returned to on Cancel
      private final Screen yaclParent;   // used to re-open ConfigScreen.create() on Save
      private final ShulkerSorterConfig config;

      /** Local working copies of patterns, keyed by category key in categoryOrder order. */
      private final Map<String, List<String>> workingPatterns = new LinkedHashMap<>();

      private int activeTabIndex = 0;
      private boolean addingWildcard = false;

      // Pane geometry (set in init)
      private int contentY;
      private int leftX, leftY, leftW, leftH;
      private int rightX, rightY, rightW, rightH;
      private int leftGridH;  // height of the scrollable item grid in the left pane

      // Scroll offsets
      private int leftScroll  = 0;
      private int rightScroll = 0;

      // Cached item lists
      private final List<ItemStack> allItems      = new ArrayList<>();
      private List<ItemStack> categoryItems = new ArrayList<>();
      private List<ItemStack> filteredItems = new ArrayList<>();
      /** Paths of items currently in the active category (for fast right-pane tint check). */
      private Set<String> categoryItemPaths = new HashSet<>();

      // Chip layout (recomputed each render frame; used for click detection)
      private record ChipRect(int x, int y, int w, int h, String pattern) {}
      private final List<ChipRect> chipRects = new ArrayList<>();

      // Widgets
      private EditBox searchBox;
      private EditBox wildcardInput;

      // -------------------------------------------------------------------------

      public CategoryEditorScreen(Screen yaclScreen, Screen yaclParent) {
          super(Component.translatable("config.shulkersorter.visual_editor.title"));
          this.yaclScreen = yaclScreen;
          this.yaclParent = yaclParent;
          this.config = ShulkerSorterConfig.getInstance();
          for (String key : config.categoryOrder) {
              CategoryDefinition def = config.categories.get(key);
              if (def != null) workingPatterns.put(key, new ArrayList<>(def.getPatterns()));
          }
      }

      @Override
      protected void init() {
          int titleBottom = 6 + 9 + 4;  // y=6, font height ~9, margin 4
          int tabBarY    = titleBottom;
          contentY       = tabBarY + TAB_H + 2;
          int contentH   = height - contentY - BOTTOM_BAR_H - 4;

          leftX = PADDING;
          leftY = contentY;
          leftW = width / 2 - PANE_GAP / 2 - PADDING;
          leftH = contentH;

          rightX = width / 2 + PANE_GAP / 2;
          rightY = contentY;
          rightW = width - rightX - PADDING;
          rightH = contentH;

          // Item grid height = left pane minus title row minus chip area
          leftGridH = leftH - 12 - CHIP_AREA_H;

          // Search box in right pane (below right pane title)
          searchBox = new EditBox(font,
                  rightX + PADDING, rightY + 12 + 2,
                  rightW - PADDING * 2, 14,
                  Component.translatable("config.shulkersorter.visual_editor.search_hint"));
          searchBox.setMaxLength(64);
          searchBox.setHint(Component.translatable("config.shulkersorter.visual_editor.search_hint"));
          searchBox.setResponder(q -> { filterItems(q); rightScroll = 0; });
          addWidget(searchBox);

          // Wildcard inline input (hidden until user clicks "+ Add Wildcard")
          wildcardInput = new EditBox(font,
                  leftX + PADDING, leftY + leftH - CHIP_AREA_H + 2,
                  leftW - PADDING * 2, 14,
                  Component.literal(""));
          wildcardInput.setMaxLength(64);
          wildcardInput.setHint(Component.translatable("config.shulkersorter.visual_editor.wildcard_hint"));
          wildcardInput.visible = false;
          addWidget(wildcardInput);

          // Save / Cancel buttons
          int btnY = height - BOTTOM_BAR_H + (BOTTOM_BAR_H - 20) / 2;
          addRenderableWidget(Button.builder(
                  Component.translatable("config.shulkersorter.visual_editor.save"),
                  btn -> onSave()
          ).bounds(width - 130, btnY, 120, 20).build());
          addRenderableWidget(Button.builder(
                  Component.translatable("config.shulkersorter.visual_editor.cancel"),
                  btn -> onCancel()
          ).bounds(width - 260, btnY, 120, 20).build());

          buildAllItems();
          refreshCategoryItems();
          filterItems("");
      }

      // ---- Helpers ---------------------------------------------------------------

      private List<String> orderedKeys() {
          return new ArrayList<>(workingPatterns.keySet());
      }

      private String activeKey() {
          List<String> keys = orderedKeys();
          if (keys.isEmpty()) return "misc";
          if (activeTabIndex >= keys.size()) activeTabIndex = 0;
          return keys.get(activeTabIndex);
      }

      private List<String> activePatterns() {
          return workingPatterns.getOrDefault(activeKey(), new ArrayList<>());
      }

      private void buildAllItems() {
          allItems.clear();
          for (Item item : BuiltInRegistries.ITEM) {
              allItems.add(item.getDefaultInstance());
          }
      }

      void refreshCategoryItems() {
          categoryItems.clear();
          categoryItemPaths.clear();
          List<String> patterns = activePatterns();
          for (ItemStack stack : allItems) {
              Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
              if (id != null && ItemCategorizer.matchesAnyPattern(id.getPath(), patterns)) {
                  categoryItems.add(stack);
                  categoryItemPaths.add(id.getPath());
              }
          }
          leftScroll = 0;
      }

      private void filterItems(String query) {
          filteredItems.clear();
          String q = query.trim().toLowerCase(java.util.Locale.ROOT);
          for (ItemStack stack : allItems) {
              if (q.isEmpty() || stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT).contains(q)) {
                  filteredItems.add(stack);
              }
          }
      }

      private int maxScroll(List<ItemStack> items, int cols, int paneH) {
          int rows = (items.size() + cols - 1) / cols;
          return Math.max(0, rows * SLOT_SIZE - paneH);
      }

      // ---- Save / Cancel ---------------------------------------------------------

      private void onSave() {
          for (Map.Entry<String, List<String>> e : workingPatterns.entrySet()) {
              CategoryDefinition def = config.categories.get(e.getKey());
              if (def != null) def.setPatterns(e.getValue());
          }
          config.save();
          // Re-open a fresh ConfigScreen so YACL re-reads the updated patterns
          minecraft.setScreen(ConfigScreen.create(yaclParent));
      }

      private void onCancel() {
          minecraft.setScreen(yaclScreen);
      }

      @Override
      public void onClose() {
          onCancel();
      }

      // ---- Render stub (filled in later tasks) -----------------------------------

      @Override
      public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
          renderBackground(g, mouseX, mouseY, partialTick);
          g.fill(0, 0, width, height, C_BG);
          // Title
          g.drawCenteredString(font,
                  Component.translatable("config.shulkersorter.visual_editor.title"),
                  width / 2, 6, 0xFFFFFFFF);
          // (tab bar, panes, chips rendered in later tasks)
          super.render(g, mouseX, mouseY, partialTick);
      }
  }
  ```

- [ ] **Step 2: Build**

  ```bash
  ./gradlew build
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

  ```bash
  git add src/client/java/com/shulkersorter/config/CategoryEditorScreen.java
  git commit -m "feat: add CategoryEditorScreen skeleton with data model and save/cancel"
  ```

---

## Task 5: Render tab bar and handle tab switching

**Files:**
- Modify: `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java`

- [ ] **Step 1: Add `renderTabBar` method**

  Add after `onClose()`:

  ```java
  private void renderTabBar(GuiGraphics g, int mouseX, int mouseY) {
      List<String> keys = orderedKeys();
      int tabBarY = contentY - TAB_H - 2;
      int x = leftX;
      int maxX = width - PADDING - 30; // leave room for "+N" overflow label
      int visibleCount = 0;

      for (int i = 0; i < keys.size(); i++) {
          String key = keys.get(i);
          CategoryDefinition def = config.categories.get(key);
          String label = (def != null)
                  ? Component.translatable(def.getLabelPrefix()).getString()
                  : key;
          boolean disabled = config.disabledCategories.contains(key);
          if (disabled) label = label + " (off)";

          int tabW = font.width(label) + 12;
          if (x + tabW > maxX) break;

          boolean active = (i == activeTabIndex);
          g.fill(x, tabBarY, x + tabW, tabBarY + TAB_H,
                  active ? C_TAB_ACTIVE : C_TAB_INACTIVE);
          g.renderOutline(x, tabBarY, tabW, TAB_H, C_TAB_BORDER);
          g.drawString(font, label, x + 6, tabBarY + (TAB_H - 9) / 2,
                  active ? 0xFFFFFFFF : (disabled ? C_TEXT_DIM : C_TEXT), false);
          x += tabW + 1;
          visibleCount++;
      }

      // Overflow indicator
      int overflow = keys.size() - visibleCount;
      if (overflow > 0) {
          String overflowText = "+" + overflow;
          g.drawString(font, overflowText, x + 4, tabBarY + (TAB_H - 9) / 2, C_TEXT_DIM, false);
      }
  }
  ```

- [ ] **Step 2: Add tab click handling in `mouseClicked`**

  Add the override:

  ```java
  @Override
  public boolean mouseClicked(double mx, double my, int button) {
      if (button == 0) {
          // Tab bar click
          List<String> keys = orderedKeys();
          int tabBarY = contentY - TAB_H - 2;
          int x = leftX;
          int maxX = width - PADDING - 30;
          for (int i = 0; i < keys.size(); i++) {
              String key = keys.get(i);
              CategoryDefinition def = config.categories.get(key);
              String label = (def != null)
                      ? Component.translatable(def.getLabelPrefix()).getString()
                      : key;
              if (config.disabledCategories.contains(key)) label = label + " (off)";
              int tabW = font.width(label) + 12;
              if (x + tabW > maxX) break;
              if (mx >= x && mx < x + tabW && my >= tabBarY && my < tabBarY + TAB_H) {
                  activeTabIndex = i;
                  addingWildcard = false;
                  wildcardInput.visible = false;
                  refreshCategoryItems();
                  rightScroll = 0;
                  return true;
              }
              x += tabW + 1;
          }
      }
      return super.mouseClicked(mx, my, button);
  }
  ```

- [ ] **Step 3: Call `renderTabBar` from `render`**

  In the `render` method, after the title draw, add:

  ```java
  renderTabBar(g, mouseX, mouseY);
  ```

- [ ] **Step 4: Build and open the screen in-game**

  ```bash
  ./gradlew runClient
  ```
  Open config via ModMenu → click "Visual Editor". Expected: dark screen with title and tab bar (no pane content yet). Click tabs — active tab highlights.

- [ ] **Step 5: Commit**

  ```bash
  git add src/client/java/com/shulkersorter/config/CategoryEditorScreen.java
  git commit -m "feat: render tab bar with category tabs and switching"
  ```

---

## Task 6: Render item grids in both panes

**Files:**
- Modify: `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java`

- [ ] **Step 1: Add `renderItemGrid` helper**

  This method renders a scrollable item grid and returns the hovered item (for tooltip). Add after `filterItems()`:

  ```java
  /**
   * Renders a scrollable item-slot grid. Returns the ItemStack under the cursor, or EMPTY.
   * @param highlightPaths  item paths to render with green slot background (already-in-category tint)
   */
  private ItemStack renderItemGrid(GuiGraphics g,
                                    List<ItemStack> items,
                                    int px, int py, int pw, int ph,
                                    int scrollOffset, int cols,
                                    Set<String> highlightPaths,
                                    int mouseX, int mouseY) {
      ItemStack hovered = ItemStack.EMPTY;
      g.fill(px, py, px + pw, py + ph, C_PANE_BG);
      g.enableScissor(px, py, px + pw, py + ph);
      for (int i = 0; i < items.size(); i++) {
          int col = i % cols;
          int row = i / cols;
          int ix = px + col * SLOT_SIZE;
          int iy = py + row * SLOT_SIZE - scrollOffset;
          if (iy + SLOT_SIZE <= py || iy >= py + ph) continue;
          ItemStack stack = items.get(i);
          Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
          boolean highlight = id != null && highlightPaths.contains(id.getPath());
          g.fill(ix, iy, ix + SLOT_SIZE, iy + SLOT_SIZE, highlight ? C_SLOT_IN : C_SLOT);
          g.renderItem(stack, ix + 2, iy + 2);
          if (mouseX >= ix && mouseX < ix + SLOT_SIZE && mouseY >= iy && mouseY < iy + SLOT_SIZE) {
              g.fill(ix, iy, ix + SLOT_SIZE, iy + SLOT_SIZE, C_SLOT_HOVER);
              hovered = stack;
          }
      }
      g.disableScissor();
      return hovered;
  }
  ```

- [ ] **Step 2: Add `renderPanes` method**

  ```java
  private ItemStack renderPanes(GuiGraphics g, int mouseX, int mouseY) {
      String key = activeKey();
      CategoryDefinition def = config.categories.get(key);
      String catLabel = (def != null)
              ? Component.translatable(def.getLabelPrefix()).getString()
              : key;

      // ---- Left pane ----
      g.fill(leftX, leftY, leftX + leftW, leftY + leftH, C_PANE_BG);
      g.drawString(font, catLabel + " — " + categoryItems.size() + " items",
              leftX + PADDING, leftY + 2, 0xFFFFFFFF, false);

      int leftCols = leftW / SLOT_SIZE;
      int leftGridY = leftY + 12;
      ItemStack leftHover = renderItemGrid(g, categoryItems,
              leftX, leftGridY, leftW, leftGridH,
              leftScroll, leftCols,
              Collections.emptySet(), // no tinting in left pane
              mouseX, mouseY);

      // ---- Right pane ----
      g.fill(rightX, rightY, rightX + rightW, rightY + rightH, C_PANE_BG);
      g.drawString(font, "All Items",
              rightX + PADDING, rightY + 2, 0xFFFFFFFF, false);

      int rightCols = rightW / SLOT_SIZE;
      int rightGridY = rightY + 12 + 16 + 4; // below title + search box
      int rightGridH = rightH - 12 - 16 - 4;
      ItemStack rightHover = renderItemGrid(g, filteredItems,
              rightX, rightGridY, rightW, rightGridH,
              rightScroll, rightCols,
              categoryItemPaths, // green tint for items already in category
              mouseX, mouseY);

      return leftHover.isEmpty() ? rightHover : leftHover;
  }
  ```

- [ ] **Step 3: Add bottom bar and wire hover tooltip into `render`**

  Replace the existing `render` method entirely:

  ```java
  @Override
  public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      renderBackground(g, mouseX, mouseY, partialTick);
      g.fill(0, 0, width, height, C_BG);

      // Title
      g.drawCenteredString(font,
              Component.translatable("config.shulkersorter.visual_editor.title"),
              width / 2, 6, 0xFFFFFFFF);

      // Tab bar
      renderTabBar(g, mouseX, mouseY);

      // Panes
      ItemStack hovered = renderPanes(g, mouseX, mouseY);

      // Chip area (Task 8 fills this in)
      renderChips(g, mouseX, mouseY);

      // Bottom bar separator
      g.fill(0, height - BOTTOM_BAR_H - 1, width, height - BOTTOM_BAR_H, C_BOTTOM_LINE);

      // Render widgets (buttons, search box, etc.)
      super.render(g, mouseX, mouseY, partialTick);

      // Hover tooltip (rendered last so it appears on top)
      if (!hovered.isEmpty()) {
          g.renderTooltip(font,
                  List.of(hovered.getHoverName(),
                          Component.literal(
                                  Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(hovered.getItem()))
                                          .toString())
                                  .withStyle(net.minecraft.ChatFormatting.GRAY)),
                  mouseX, mouseY);
      }
  }
  ```

  Add the missing import at the top of the file:
  ```java
  import java.util.Objects;
  ```

- [ ] **Step 4: Add a stub `renderChips` (filled in Task 8)**

  ```java
  private void renderChips(GuiGraphics g, int mouseX, int mouseY) {
      // implemented in Task 8
  }
  ```

- [ ] **Step 5: Build and verify**

  ```bash
  ./gradlew runClient
  ```
  Expected: both panes show item icons. Right pane items that match the active category have a green slot. Hovering an item shows a tooltip with its name and ID. Search box filters the right pane.

- [ ] **Step 6: Commit**

  ```bash
  git add src/client/java/com/shulkersorter/config/CategoryEditorScreen.java
  git commit -m "feat: render item grids in both panes with hover tooltip and search"
  ```

---

## Task 7: Mouse scroll and click interactions

**Files:**
- Modify: `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java`

- [ ] **Step 1: Add `mouseScrolled`**

  ```java
  @Override
  public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
      int leftCols  = leftW / SLOT_SIZE;
      int rightCols = rightW / SLOT_SIZE;
      int rightGridH = rightH - 12 - 16 - 4;

      // Left pane scroll
      if (mx >= leftX && mx < leftX + leftW && my >= leftY && my < leftY + leftH) {
          leftScroll = (int) Math.max(0, Math.min(
                  maxScroll(categoryItems, leftCols, leftGridH),
                  leftScroll - scrollY * SLOT_SIZE));
          return true;
      }
      // Right pane scroll
      if (mx >= rightX && mx < rightX + rightW && my >= rightY && my < rightY + rightH) {
          rightScroll = (int) Math.max(0, Math.min(
                  maxScroll(filteredItems, rightCols, rightGridH),
                  rightScroll - scrollY * SLOT_SIZE));
          return true;
      }
      return super.mouseScrolled(mx, my, scrollX, scrollY);
  }
  ```

- [ ] **Step 2: Add item click helpers**

  ```java
  private ItemStack getItemAt(List<ItemStack> items,
                               int px, int py, int pw, int ph,
                               int scrollOffset, int cols,
                               int mouseX, int mouseY) {
      if (mouseX < px || mouseX >= px + pw || mouseY < py || mouseY >= py + ph) return ItemStack.EMPTY;
      int col = (mouseX - px) / SLOT_SIZE;
      int row = (mouseY - py + scrollOffset) / SLOT_SIZE;
      int idx = row * cols + col;
      if (col >= cols || idx < 0 || idx >= items.size()) return ItemStack.EMPTY;
      return items.get(idx);
  }
  ```

- [ ] **Step 3: Wire clicks in `mouseClicked`**

  In the existing `mouseClicked` override, add the item-grid click handling **before** the `return super.mouseClicked(...)` line:

  ```java
  if (button == 0) {
      // ... existing tab bar code ...

      int leftCols  = leftW / SLOT_SIZE;
      int rightCols = rightW / SLOT_SIZE;
      int leftGridY  = leftY + 12;
      int rightGridY = rightY + 12 + 16 + 4;
      int rightGridH = rightH - 12 - 16 - 4;

      // Left pane click: remove exact-ID pattern
      ItemStack leftClicked = getItemAt(categoryItems,
              leftX, leftGridY, leftW, leftGridH, leftScroll, leftCols,
              (int) mx, (int) my);
      if (!leftClicked.isEmpty()) {
          Identifier id = BuiltInRegistries.ITEM.getKey(leftClicked.getItem());
          if (id != null) {
              activePatterns().remove(id.getPath());
              refreshCategoryItems();
          }
          return true;
      }

      // Right pane click: add exact-ID pattern
      ItemStack rightClicked = getItemAt(filteredItems,
              rightX, rightGridY, rightW, rightGridH, rightScroll, rightCols,
              (int) mx, (int) my);
      if (!rightClicked.isEmpty()) {
          Identifier id = BuiltInRegistries.ITEM.getKey(rightClicked.getItem());
          if (id != null && !activePatterns().contains(id.getPath())) {
              activePatterns().add(id.getPath());
              refreshCategoryItems();
          }
          return true;
      }

      // Chip clicks are handled in Task 8
  }
  ```

- [ ] **Step 4: Build and test**

  ```bash
  ./gradlew runClient
  ```
  Expected:
  - Clicking a right-pane item adds it to the category (appears in left pane immediately, slot turns green in right pane).
  - Clicking a left-pane item whose ID is an exact pattern removes it (disappears from left pane).
  - Left-pane items that matched via wildcard (not exact ID) are not removed when clicked — they simply don't disappear because there's no exact-ID pattern to remove.
  - Mouse wheel scrolls each pane independently.

- [ ] **Step 5: Commit**

  ```bash
  git add src/client/java/com/shulkersorter/config/CategoryEditorScreen.java
  git commit -m "feat: wire item grid scroll and click interactions"
  ```

---

## Task 8: Wildcard chips + add-wildcard EditBox

**Files:**
- Modify: `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java`

- [ ] **Step 1: Replace the stub `renderChips` with a real implementation**

  ```java
  private void renderChips(GuiGraphics g, int mouseX, int mouseY) {
      chipRects.clear();
      if (addingWildcard) {
          // While adding, only show the EditBox (already a widget) — no chips
          // Draw a subtle label above it
          g.drawString(font, "New wildcard pattern:",
                  leftX + PADDING, wildcardInput.getY() - 11, C_TEXT_DIM, false);
          // Confirm hint
          g.drawString(font, "[Enter] confirm  [Esc] cancel",
                  leftX + PADDING, wildcardInput.getY() + 16, C_TEXT_DIM, false);
          return;
      }

      int startX = leftX + PADDING;
      int startY = leftY + 12 + leftGridH + 4;
      int maxW   = leftW - PADDING * 2;
      int x = startX;
      int y = startY;
      int lineH = 12;

      for (String pattern : activePatterns()) {
          String chipText = pattern + " ×"; // × symbol
          int textW = font.width(chipText);
          int chipW = textW + 8;
          if (x + chipW > startX + maxW && x > startX) { x = startX; y += lineH + 2; }
          g.fill(x, y, x + chipW, y + lineH, C_CHIP_BG);
          g.renderOutline(x, y, chipW, lineH, C_CHIP_BORDER);
          g.drawString(font, chipText, x + 4, y + 2, 0xFF88FF88, false);
          chipRects.add(new ChipRect(x, y, chipW, lineH, pattern));
          x += chipW + 3;
      }

      // "+ Add Wildcard" button chip
      String addLabel = Component.translatable("config.shulkersorter.visual_editor.add_wildcard").getString();
      int addW = font.width(addLabel) + 8;
      if (x + addW > startX + maxW && x > startX) { x = startX; y += lineH + 2; }
      g.fill(x, y, x + addW, y + lineH, 0xFF1a2a1a);
      g.renderOutline(x, y, addW, lineH, 0xFF446644);
      g.drawString(font, addLabel, x + 4, y + 2, 0xFF668866, false);
      chipRects.add(new ChipRect(x, y, addW, lineH, null)); // null pattern = "add" button
  }
  ```

- [ ] **Step 2: Add chip click detection in `mouseClicked`**

  In `mouseClicked`, **after** the right-pane item click block and **before** `return super.mouseClicked(...)`:

  ```java
  // Chip clicks
  if (button == 0) {
      for (ChipRect chip : chipRects) {
          if (mx >= chip.x() && mx < chip.x() + chip.w()
                  && my >= chip.y() && my < chip.y() + chip.h()) {
              if (chip.pattern() == null) {
                  // "Add Wildcard" chip
                  addingWildcard = true;
                  wildcardInput.visible = true;
                  wildcardInput.setFocused(true);
                  wildcardInput.setValue("");
              } else {
                  // Remove pattern chip
                  activePatterns().remove(chip.pattern());
                  refreshCategoryItems();
              }
              return true;
          }
      }
  }
  ```

- [ ] **Step 3: Handle Enter / Escape for the wildcard EditBox**

  Add the `keyPressed` override:

  ```java
  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (addingWildcard && wildcardInput.isFocused()) {
          if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
                  || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
              String newPattern = wildcardInput.getValue().trim();
              if (!newPattern.isEmpty() && !activePatterns().contains(newPattern)) {
                  activePatterns().add(newPattern);
                  refreshCategoryItems();
              }
              wildcardInput.visible = false;
              addingWildcard = false;
              return true;
          }
          if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
              wildcardInput.visible = false;
              addingWildcard = false;
              return true;
          }
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
  }
  ```

  Add the import at top of file:
  ```java
  import org.lwjgl.glfw.GLFW;
  ```

- [ ] **Step 4: Build and test**

  ```bash
  ./gradlew runClient
  ```
  Expected:
  - Wildcard chips appear below the left item grid (e.g. `repeater ×`, `_detector ×`).
  - Clicking a chip's × removes that pattern; items matched only by that pattern disappear from the left pane.
  - Clicking "+ Add Wildcard" shows an EditBox. Typing `_planks` and pressing Enter adds the pattern and all plank variants appear in the left pane. Esc cancels.

- [ ] **Step 5: Commit**

  ```bash
  git add src/client/java/com/shulkersorter/config/CategoryEditorScreen.java
  git commit -m "feat: render wildcard chips with remove and add-wildcard flow"
  ```

---

## Task 9: Save, verify, and wrap up

**Files:**
- Modify: `src/client/java/com/shulkersorter/config/CategoryEditorScreen.java`

- [ ] **Step 1: Build a clean release JAR**

  ```bash
  ./gradlew build
  ```
  Expected: `BUILD SUCCESSFUL`. JAR in `build/libs/`.

- [ ] **Step 2: End-to-end in-game test**

  ```bash
  ./gradlew runClient
  ```

  Test checklist:
  - Open config (ModMenu) → Categories tab → "Visual Editor" button is visible → click it
  - Editor opens with tabs for all categories, Redstone tab active
  - Items in Redstone category render with green slot in right pane; left pane shows all matched items
  - Search `plank` in right pane → filters to plank variants → click one → it appears in left pane with green tint
  - Click the just-added item in the left pane → it disappears (exact-ID pattern removed)
  - Click `repeater ×` wildcard chip → all repeater variants disappear from left pane
  - Click `+ Add Wildcard`, type `observer`, press Enter → observer appears in left pane
  - Switch to Nature tab → left pane updates to Nature items
  - Click Save & Close → returns to YACL config screen with updated patterns visible in Patterns list
  - Close YACL → re-open → patterns persisted in TOML

- [ ] **Step 3: Commit**

  ```bash
  git add -A
  git commit -m "feat: visual category editor complete"
  ```
