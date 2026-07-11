package de.dennisthegamer.shulkersorter.hud;

import de.dennisthegamer.shulkersorter.config.ShulkerSorterConfig;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class SortingHudOverlay {
    private static long showUntil = 0;
    private static final long DISPLAY_DURATION_MS = 1000;

    public static void register() {
        ClientGuiEvent.RENDER_HUD.register(SortingHudOverlay::render);
    }

    public static void show() {
        if (ShulkerSorterConfig.getInstance().enableHudOverlay) {
            showUntil = System.currentTimeMillis() + DISPLAY_DURATION_MS;
        }
    }

    private static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        long now = System.currentTimeMillis();
        if (now >= showUntil) return;

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;

        // Animated dots
        int dots = (int) ((now / 300) % 4);
        String dotStr = ".".repeat(dots);
        String sortingText = Component.translatable("shulkersorter.hud.sorting").getString();
        String text = ChatFormatting.YELLOW + sortingText + dotStr;

        int screenWidth = guiGraphics.guiWidth();
        int x = (screenWidth - font.width(sortingText + "...")) / 2;
        int y = 20;

        guiGraphics.text(font, text, x, y, 0xFFFFAA00);
    }
}
