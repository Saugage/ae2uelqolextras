package com.ae2qolextras.container;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.util.Platform;
import com.ae2qolextras.part.PartActiveFormationPlane;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerActiveFormationPlane extends ContainerFormationPlane {

    public ContainerActiveFormationPlane(final InventoryPlayer ip, final PartActiveFormationPlane te) {
        super(ip, te);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            final IConfigManager cm = this.getUpgradeable().getConfigManager();
            this.setRedStoneMode((RedstoneMode) cm.getSetting(Settings.REDSTONE_CONTROLLED));
            this.setCraftingMode((YesNo) cm.getSetting(Settings.CRAFT_ONLY));
        }
        super.detectAndSendChanges();
    }
}
