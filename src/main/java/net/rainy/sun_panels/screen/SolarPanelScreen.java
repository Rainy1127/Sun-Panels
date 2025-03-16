package net.rainy.sun_panels.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.rainy.sun_panels.Sun_Panels;
import net.rainy.sun_panels.menu.SolarPanelMenu;

public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Sun_Panels.MOD_ID, "textures/gui/solar_panel.png");

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // The width and height of the GUI
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw the background
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Draw the energy bar
        renderEnergyBar(guiGraphics, x, y);

        // Draw the generation bar
        renderGenerationBar(guiGraphics, x, y);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        // Energy bar position
        int energyX = x + 154;
        int energyY = y + 18;

        // Get energy data from the menu
        int energyStored = menu.getEnergyStored();
        int maxEnergy = menu.getMaxEnergyStored();

        // Calculate the height of the energy bar based on stored energy
        int energyBarHeight = 42;
        int filledHeight = maxEnergy > 0 ? (int)(((float)energyStored / maxEnergy) * energyBarHeight) : 0;

        // Draw the energy bar background
        guiGraphics.blit(TEXTURE, energyX, energyY, 176, 0, 14, energyBarHeight);

        // Draw the filled portion of the energy bar (from bottom to top)
        if (filledHeight > 0) {
            guiGraphics.blit(TEXTURE, energyX, energyY + energyBarHeight - filledHeight, 176, energyBarHeight - filledHeight, 14, filledHeight);
        }
    }

    private void renderGenerationBar(GuiGraphics guiGraphics, int x, int y) {
        // Generation bar position
        int genX = x + 134;
        int genY = y + 18;

        // Get generation data from the menu
        int currentGeneration = menu.getCurrentGeneration();
        int maxGeneration = menu.getMaxGeneration();

        // Calculate the height of the generation bar
        int genBarHeight = 42;
        int filledHeight = maxGeneration > 0 ? (int)(((float)currentGeneration / maxGeneration) * genBarHeight) : 0;

        // Draw the generation bar background
        guiGraphics.blit(TEXTURE, genX, genY, 190, 0, 14, genBarHeight);

        // Draw the filled portion of the generation bar (from bottom to top)
        if (filledHeight > 0) {
            guiGraphics.blit(TEXTURE, genX, genY + genBarHeight - filledHeight, 190, genBarHeight - filledHeight, 14, filledHeight);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Energy tooltip
        if (isHovering(154, 18, 14, 42, mouseX, mouseY)) {
            int energyStored = menu.getEnergyStored();
            int maxEnergy = menu.getMaxEnergyStored();
            guiGraphics.renderTooltip(this.font, Component.literal(energyStored + " / " + maxEnergy + " FE"), mouseX, mouseY);
        }

        // Generation tooltip
        if (isHovering(134, 18, 14, 42, mouseX, mouseY)) {
            int currentGeneration = menu.getCurrentGeneration();
            guiGraphics.renderTooltip(this.font, Component.literal("Generating: " + currentGeneration + " FE/t"), mouseX, mouseY);
        }
    }
}