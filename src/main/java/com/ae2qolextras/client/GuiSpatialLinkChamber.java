package com.ae2qolextras.client;

import com.ae2qolextras.Tags;
import com.ae2qolextras.container.ContainerSpatialLinkChamber;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiSpatialLinkChamber extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MODID, "textures/guis/spatialchamber.png");

    public GuiSpatialLinkChamber(final ContainerSpatialLinkChamber container) {
        super(container);
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(ContainerSpatialLinkChamber.BUTTON_TELEPORT_INSIDE, this.guiLeft + 132, this.guiTop + 35, 38, 20,
                I18n.format("gui.ae2qolextras.teleport_inside")));
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button.id == ContainerSpatialLinkChamber.BUTTON_TELEPORT_INSIDE) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id);
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
}
