package net.rainy.sun_panels.client.gui;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.rainy.sun_panels.block.entity.BaseSolarPanelBlockEntity;
import net.rainy.sun_panels.menu.SolarPanelMenu;

/**
 * Tech-minimalist dark status screen: OLED black panel, neon cyan for stored energy and sunlight,
 * neon orange for the current output, neon red when the panel cannot see the sky. Drawn entirely in code.
 */
public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    private static final int IMAGE_W = 176;
    private static final int IMAGE_H = 166;

    private static final int PANEL_BG = 0xFF0A0A0E;
    private static final int PANEL_EDGE = 0xFF1C1C26;
    private static final int TITLE_BG = 0xFF101016;
    private static final int CARD_BG = 0xFF12121A;
    private static final int TRACK_BG = 0xFF000000;
    private static final int SLOT_BG = 0xFF08080B;
    private static final int SLOT_EDGE = 0xFF2A2A36;
    private static final int NEON_CYAN = 0xFF00FFFF;
    private static final int CYAN_DIM = 0xFF0B7A7A;
    private static final int NEON_ORANGE = 0xFFFF9F1C;
    private static final int NEON_RED = 0xFFFF3B3B;
    private static final int TEXT_GRAY = 0xFF7A7A88;

    private static final int BAR_X = 10;
    private static final int BAR_Y = 22;
    private static final int BAR_W = 14;
    private static final int BAR_H = 54;

    private static final String[] TIER_KEYS = {
            "block.sun_panels.basic_solar_panel",
            "block.sun_panels.advanced_solar_panel",
            "block.sun_panels.elite_solar_panel"
    };

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = IMAGE_W;
        this.imageHeight = IMAGE_H;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int x0 = leftPos + BAR_X;
        int y0 = topPos + BAR_Y;
        if (mouseX >= x0 && mouseX < x0 + BAR_W && mouseY >= y0 && mouseY < y0 + BAR_H) {
            g.renderTooltip(this.font, Component.translatable("gui.sun_panels.energy_tooltip",
                    format(menu.energy()), format(menu.capacity())), mouseX, mouseY);
        } else {
            renderTooltip(g, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        final int x = leftPos;
        final int y = topPos;

        g.fill(x, y, x + IMAGE_W, y + IMAGE_H, PANEL_BG);
        frame(g, x, y, x + IMAGE_W, y + IMAGE_H, PANEL_EDGE);
        g.fill(x + 1, y + 1, x + IMAGE_W - 1, y + 15, TITLE_BG);
        g.fill(x + 1, y + 15, x + IMAGE_W - 1, y + 16, CYAN_DIM);
        int tier = Math.max(0, Math.min(TIER_KEYS.length - 1, menu.tierIndex()));
        text(g, Component.translatable(TIER_KEYS[tier]), x + 8, y + 4, NEON_CYAN);

        drawEnergyBar(g, x, y);
        drawStatusCard(g, x, y);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotWell(g, x + 7 + col * 18, y + SolarPanelMenu.INVENTORY_Y - 1 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slotWell(g, x + 7 + col * 18, y + SolarPanelMenu.INVENTORY_Y - 1 + 58);
        }
    }

    private void drawEnergyBar(GuiGraphics g, int x, int y) {
        int x0 = x + BAR_X;
        int y0 = y + BAR_Y;
        g.fill(x0, y0, x0 + BAR_W, y0 + BAR_H, TRACK_BG);
        frame(g, x0 - 1, y0 - 1, x0 + BAR_W + 1, y0 + BAR_H + 1, PANEL_EDGE);

        int capacity = menu.capacity();
        int filled = capacity <= 0 ? 0 : (int) ((long) BAR_H * menu.energy() / capacity);
        if (filled > 0) {
            g.fill(x0, y0 + BAR_H - filled, x0 + BAR_W, y0 + BAR_H, NEON_CYAN);
        }
        for (int tick = 1; tick < 4; tick++) {
            int ty = y0 + BAR_H * tick / 4;
            g.fill(x0, ty, x0 + 3, ty + 1, PANEL_EDGE);
        }
    }

    private void drawStatusCard(GuiGraphics g, int x, int y) {
        int left = x + 32;
        int right = x + 168;
        int top = y + 21;
        g.fill(left, top, right, top + 56, CARD_BG);
        frame(g, left, top, right, top + 56, PANEL_EDGE);
        int inner = right - left - 12;

        int output = menu.output();
        int max = menu.maxOutput();
        labelValue(g, Component.translatable("gui.sun_panels.stored"),
                Component.translatable("gui.sun_panels.rf", format(menu.energy())), left + 6, right - 6, top + 5, NEON_CYAN);
        labelValue(g, Component.translatable("gui.sun_panels.output"),
                Component.translatable("gui.sun_panels.rf_t", format(output), format(max)), left + 6, right - 6, top + 16,
                output > 0 ? NEON_ORANGE : TEXT_GRAY);

        int barX0 = left + 6;
        int barX1 = right - 6;
        int barY = top + 28;
        g.fill(barX0, barY, barX1, barY + 3, TRACK_BG);
        int filled = max <= 0 ? 0 : (int) ((long) (barX1 - barX0) * output / max);
        if (filled > 0) {
            g.fill(barX0, barY, barX0 + filled, barY + 3, NEON_ORANGE);
        }

        Component status;
        int color;
        switch (menu.skyState()) {
            case BaseSolarPanelBlockEntity.SKY_SUN -> {
                status = Component.translatable("gui.sun_panels.sky.sun");
                color = NEON_CYAN;
            }
            case BaseSolarPanelBlockEntity.SKY_DARK -> {
                status = Component.translatable("gui.sun_panels.sky.dark");
                color = TEXT_GRAY;
            }
            default -> {
                status = Component.translatable("gui.sun_panels.sky.blocked");
                color = NEON_RED;
            }
        }
        textFit(g, status, left + 6, top + 36, color, inner);
        if (menu.skyState() == BaseSolarPanelBlockEntity.SKY_BLOCKED) {
            textFit(g, Component.translatable("gui.sun_panels.hint.blocked"), left + 6, top + 46, TEXT_GRAY, inner);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // The panel draws its own title; the vanilla "Inventory" label is not needed.
    }

    /** Label on the left, value right-aligned; the label shrinks if both do not fit on one line. */
    private void labelValue(GuiGraphics g, Component label, Component value, int left, int right, int y, int valueColor) {
        int valueWidth = this.font.width(value);
        text(g, value, right - valueWidth, y, valueColor);
        textFit(g, label, left, y, TEXT_GRAY, Math.max(10, right - valueWidth - 4 - left));
    }

    /** Draws text, scaled down when it is wider than {@code maxWidth} (long translations). */
    private void textFit(GuiGraphics g, Component text, int x, int y, int color, int maxWidth) {
        int width = this.font.width(text);
        if (width <= maxWidth) {
            text(g, text, x, y, color);
            return;
        }
        float scale = (float) maxWidth / width;
        g.pose().pushPose();
        g.pose().translate(x, y + (1.0F - scale) * 4.0F, 0.0F);
        g.pose().scale(scale, scale, 1.0F);
        g.drawString(this.font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private static String format(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private void text(GuiGraphics g, Component text, int x, int y, int color) {
        g.drawString(this.font, text, x, y, color, false);
    }

    private static void frame(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        g.fill(x0, y0, x1, y0 + 1, color);
        g.fill(x0, y1 - 1, x1, y1, color);
        g.fill(x0, y0 + 1, x0 + 1, y1 - 1, color);
        g.fill(x1 - 1, y0 + 1, x1, y1 - 1, color);
    }

    private static void slotWell(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, SLOT_BG);
        frame(g, x, y, x + 18, y + 18, SLOT_EDGE);
    }
}
