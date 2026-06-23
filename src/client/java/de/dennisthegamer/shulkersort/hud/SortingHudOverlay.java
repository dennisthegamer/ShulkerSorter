package de.dennisthegamer.shulkersort.hud;

import de.dennisthegamer.shulkersort.config.ShulkerSortConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SortingHudOverlay {
    private static long showUntil = 0;
    private static final long DISPLAY_DURATION_MS = 1000;

    public static void register() {
        HudRenderCallback.EVENT.register(SortingHudOverlay::onHudRender);
    }

    public static void show() {
        if (ShulkerSortConfig.getInstance().enableHudOverlay) {
            showUntil = System.currentTimeMillis() + DISPLAY_DURATION_MS;
        }
    }

    private static void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        long now = System.currentTimeMillis();
        if (now >= showUntil) return;

        MinecraftClient client = MinecraftClient.getInstance();

        TextRenderer textRenderer = client.textRenderer;

        // Animated dots
        int dots = (int) ((now / 300) % 4);
        String dotStr = ".".repeat(dots);
        String sortingText = Text.translatable("shulkersort.hud.sorting").getString();
        String text = Formatting.YELLOW + sortingText + dotStr;

        int screenWidth = client.getWindow().getScaledWidth();
        int x = (screenWidth - textRenderer.getWidth(sortingText + "...")) / 2;
        int y = 20;

        context.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFAA00);
    }
}
