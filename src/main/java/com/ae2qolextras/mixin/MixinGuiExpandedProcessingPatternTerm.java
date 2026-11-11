package com.ae2qolextras.mixin;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiExpandedProcessingPatternTerm;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.slot.SlotFakeCraftingMatrix;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static appeng.helpers.PatternHelper.CRAFTING_GRID_DIMENSION;

@Mixin(value = GuiExpandedProcessingPatternTerm.class, remap = false)
public abstract class MixinGuiExpandedProcessingPatternTerm extends GuiMEMonitorable {

    private static final String BACKGROUND_CRAFTING_MODE = "guis/pattern.png";

    @Shadow
    private GuiTabButton tabCraftButton;
    @Shadow
    private GuiTabButton tabProcessButton;
    @Shadow
    private GuiImgButton substitutionsEnabledBtn;
    @Shadow
    private GuiImgButton substitutionsDisabledBtn;
    @Shadow
    private GuiImgButton x2Btn;
    @Shadow
    private GuiImgButton x3Btn;
    @Shadow
    private GuiImgButton plusOneBtn;
    @Shadow
    private GuiImgButton divTwoBtn;
    @Shadow
    private GuiImgButton divThreeBtn;
    @Shadow
    private GuiImgButton minusOneBtn;

    private MixinGuiExpandedProcessingPatternTerm(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
    }

    private boolean ae2qolextras$isCraftingMode() {
        return ((ContainerPatternEncoder) this.inventorySlots).isCraftingMode();
    }

    @Inject(method = "drawFG", at = @At(value = "INVOKE", target = "Lappeng/client/gui/implementations/GuiMEMonitorable;drawFG(IIII)V"))
    private void ae2qolextras$updateButtons(int offsetX, int offsetY, int mouseX, int mouseY, CallbackInfo ci) {
        final boolean crafting = this.ae2qolextras$isCraftingMode();
        final boolean substitute = ((ContainerPatternEncoder) this.inventorySlots).substitute;

        this.tabCraftButton.visible = crafting;
        this.tabProcessButton.visible = !crafting;
        this.substitutionsEnabledBtn.visible = crafting && substitute;
        this.substitutionsDisabledBtn.visible = crafting && !substitute;
        this.x2Btn.visible = !crafting;
        this.x3Btn.visible = !crafting;
        this.divTwoBtn.visible = !crafting;
        this.divThreeBtn.visible = !crafting;
        this.plusOneBtn.visible = !crafting;
        this.minusOneBtn.visible = !crafting;
    }

    // getBackground runs from GuiMEMonitorable.drawBG, which NAE2's Pattern Multi Tool still calls. Overwriting
    // drawBG here would be dropped or would drop that panel, and the 4x4 grid from initGui would stay on screen.
    @Inject(method = "getBackground", at = @At("HEAD"), cancellable = true)
    private void ae2qolextras$getBackground(CallbackInfoReturnable<String> cir) {
        this.ae2qolextras$layoutCraftingGrid();
        if (this.ae2qolextras$isCraftingMode()) {
            cir.setReturnValue(BACKGROUND_CRAFTING_MODE);
        }
    }

    // Must match the y offset applied by GuiExpandedProcessingPatternTerm.repositionSlot.
    private void ae2qolextras$layoutCraftingGrid() {
        final boolean crafting = this.ae2qolextras$isCraftingMode();
        final int yOffset = this.ySize - 78 - 3;

        for (final Slot slot : this.inventorySlots.inventorySlots) {
            if (!(slot instanceof SlotFakeCraftingMatrix)) {
                continue;
            }

            final SlotFakeCraftingMatrix s = (SlotFakeCraftingMatrix) slot;
            final int idx = s.getSlotIndex();

            if (!crafting) {
                s.xPos = s.getX();
                s.yPos = s.getY() + yOffset;
            } else if (idx < CRAFTING_GRID_DIMENSION * CRAFTING_GRID_DIMENSION) {
                s.xPos = 18 + (idx % CRAFTING_GRID_DIMENSION) * 18;
                s.yPos = -76 + (idx / CRAFTING_GRID_DIMENSION) * 18 + yOffset;
            } else {
                s.xPos = -9000;
            }
        }
    }
}
