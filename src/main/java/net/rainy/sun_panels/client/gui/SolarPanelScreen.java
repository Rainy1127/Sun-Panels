package net.rainy.sun_panels.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.rainy.sun_panels.Sun_Panels;
import net.rainy.sun_panels.menu.SolarPanelMenu;

public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Sun_Panels.MOD_ID, "textures/gui/solar_panel.png");
    private static final int ENERGY_BAR_WIDTH = 14;
    private static final int ENERGY_BAR_HEIGHT = 42;
    private static final int ENERGY_BAR_X = 154;
    private static final int ENERGY_BAR_Y = 18;
    private static final int GENERATION_BAR_WIDTH = 14;
    private static final int GENERATION_BAR_HEIGHT = 42;
    private static final int GENERATION_BAR_X = 134;
    private static final int GENERATION_BAR_Y = 18;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Render the background texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render the energy bar
        int energyStored = this.menu.getEnergyStored();
        int maxEnergy = this.menu.getMaxEnergyStored();
        int energyBarHeight = maxEnergy > 0 ? (int) (((float) energyStored / maxEnergy) * ENERGY_BAR_HEIGHT) : 0;

        if (energyBarHeight > 0) {
            guiGraphics.blit(TEXTURE, x + ENERGY_BAR_X, y + ENERGY_BAR_Y + (ENERGY_BAR_HEIGHT - energyBarHeight),
                    176, 0, ENERGY_BAR_WIDTH, energyBarHeight);
        }

        // Render the generation bar
        int currentGeneration = this.menu.getCurrentGeneration();
        int maxGeneration = this.menu.getMaxGeneration();
        int generationBarHeight = maxGeneration > 0 ? (int) (((float) currentGeneration / maxGeneration) * GENERATION_BAR_HEIGHT) : 0;

        if (generationBarHeight > 0) {
            guiGraphics.blit(TEXTURE, x + GENERATION_BAR_X, y + GENERATION_BAR_Y + (GENERATION_BAR_HEIGHT - generationBarHeight),
                    190, 0, GENERATION_BAR_WIDTH, generationBarHeight);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // Render energy tooltip
        if (isHovering(ENERGY_BAR_X, ENERGY_BAR_Y, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui." + Sun_Panels.MOD_ID + ".energy",
                    this.menu.getEnergyStored(), this.menu.getMaxEnergyStored()), mouseX, mouseY);
        }

        // Render generation tooltip
        if (isHovering(GENERATION_BAR_X, GENERATION_BAR_Y, GENERATION_BAR_WIDTH, GENERATION_BAR_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui." + Sun_Panels.MOD_ID + ".generation",
                    this.menu.getCurrentGeneration(), this.menu.getMaxGeneration()), mouseX, mouseY);
        }
    }
}