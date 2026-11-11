package com.ae2qolextras.mixin;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerExpandedProcessingPatternTerm;
import appeng.container.implementations.ContainerPatternEncoder;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotPatternTerm;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

import static appeng.helpers.PatternHelper.CRAFTING_GRID_DIMENSION;

@Mixin(value = ContainerExpandedProcessingPatternTerm.class, remap = false)
public abstract class MixinContainerExpandedProcessingPatternTerm extends ContainerPatternEncoder {

    private static final int OUTPUT_GROUP = 1;
    private static final int CRAFT_GROUP = 2;

    private MixinContainerExpandedProcessingPatternTerm(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable, false);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2qolextras$addCraftSlot(InventoryPlayer ip, ITerminalHost monitorable, CallbackInfo ci) {
        this.craftSlot = new SlotPatternTerm(ip.player, this.getActionSource(), this.getPowerSource(), monitorable, this.crafting,
                this.getPart().getInventoryByName("pattern"), ((ContainerPatternEncoderAccessor) this).ae2qolextras$getCOut(),
                110, -76 + 18, this, CRAFT_GROUP, this);
        this.addSlotToContainer(this.craftSlot);
        this.craftSlot.setIIcon(-1);
        this.craftingMode = this.getPart().isCraftingRecipe();
        this.updateOrderOfOutputSlots();
    }

    @Inject(method = "isSlotEnabled", at = @At("HEAD"), cancellable = true)
    private void ae2qolextras$isSlotEnabled(int idx, CallbackInfoReturnable<Boolean> cir) {
        if (idx == OUTPUT_GROUP || idx == CRAFT_GROUP) {
            final boolean crafting = Platform.isServer() ? this.getPart().isCraftingRecipe() : this.craftingMode;
            cir.setReturnValue(idx == CRAFT_GROUP ? crafting : !crafting);
        }
    }

    @Inject(method = "isCraftingMode", at = @At("HEAD"), cancellable = true)
    private void ae2qolextras$isCraftingMode(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.craftingMode);
    }

    @Override
    protected void updateOrderOfOutputSlots() {
        final boolean crafting = this.isCraftingMode();

        if (this.craftSlot != null) {
            this.craftSlot.xPos = crafting ? this.craftSlot.getX() : -9000;
        }

        for (final OptionalSlotFake outputSlot : this.outputSlots) {
            outputSlot.xPos = crafting ? -9000 : outputSlot.getX();
        }
    }

    @Override
    protected ItemStack[] getInputs() {
        final ItemStack[] inputs = super.getInputs();

        if (inputs == null || !this.isCraftingMode()) {
            return inputs;
        }

        return Arrays.copyOf(inputs, CRAFTING_GRID_DIMENSION * CRAFTING_GRID_DIMENSION);
    }
}
