package com.ae2qolextras.client;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import com.ae2qolextras.container.ContainerActiveFormationPlane;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiActiveFormationPlane extends GuiUpgradeable {

    private GuiImgButton placeMode;

    public GuiActiveFormationPlane(final ContainerActiveFormationPlane container) {
        super(container);
        this.ySize = 251;
    }

    @Override
    protected void addButtons() {
        this.placeMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.PLACE_BLOCK, YesNo.YES);
        this.redstoneMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 28, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.fuzzyMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 48, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.craftMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 68, Settings.CRAFT_ONLY, YesNo.NO);

        this.buttonList.add(this.placeMode);
        this.buttonList.add(this.redstoneMode);
        this.buttonList.add(this.fuzzyMode);
        this.buttonList.add(this.craftMode);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(I18n.format("gui.ae2qolextras.active_formation_plane")), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        this.redstoneMode.set(this.cvb.getRedStoneMode());
        this.fuzzyMode.set(this.cvb.getFuzzyMode());
        this.craftMode.set(this.cvb.getCraftingMode());
        this.placeMode.set(((ContainerActiveFormationPlane) this.cvb).getPlaceMode());
    }

    @Override
    protected String getBackground() {
        return "guis/storagebus.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if (btn == this.placeMode) {
            NetworkHandler.instance().sendToServer(new PacketConfigButton(this.placeMode.getSetting(), Mouse.isButtonDown(1)));
        }
    }
}
