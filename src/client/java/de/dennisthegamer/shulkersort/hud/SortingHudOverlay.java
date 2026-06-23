package de.dennisthegamer.shulkersort.hud;

import de.dennisthegamer.shulkersort.config.ShulkerSortConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SortingHudOverlay implements HudElement {
    private static long showUntil = 0;
    private static final long DISPLAY_DURATION_MS = 1000;

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("shulkersort", "sorting_overlay"),
                new SortingHudOverlay()
        );
    }

    public static void show() {
        if (ShulkerSortConfig.getInstance().enableHudOverlay) {
            showUntil = System.currentTimeMillis() + DISPLAY_DURATION_MS;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        long now = System.currentTimeMillis();
        if (now >= showUntil) return;

        Minecraft client = Minecraft.getInstance();
        if (client == null) return;

        Font font = client.font;

        // Animated dots
        int dots = (int) ((now / 300) % 4);
        String dotStr = ".".repeat(dots);
        String sortingText = Component.translatable("shulkersort.hud.sorting").getString();
        String text = ChatFormatting.YELLOW + sortingText + dotStr;

        int screenWidth = guiGraphics.guiWidth();
        int x = (screenWidth - font.width(sortingText + "...")) / 2;
        int y = 20;

        guiGraphics.text(font, text, x, y, 0xFFFFAA00);
    }
}
