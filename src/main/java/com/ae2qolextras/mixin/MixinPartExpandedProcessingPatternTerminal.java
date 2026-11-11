package com.ae2qolextras.mixin;

import appeng.parts.reporting.AbstractPartEncoder;
import com.ae2qolextras.compat.Ae2fcCompat;
import appeng.parts.reporting.PartExpandedProcessingPatternTerminal;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static appeng.helpers.PatternHelper.CRAFTING_GRID_DIMENSION;

// AE2 Fluid Crafting adds the same NBT/inventory overrides (its readFromNBT never loads the base inventories), so these
// must always win; Ae2fcCompat re-applies what its versions add.
@Mixin(value = PartExpandedProcessingPatternTerminal.class, remap = false, priority = 1100)
public abstract class MixinPartExpandedProcessingPatternTerminal extends AbstractPartEncoder {

    private MixinPartExpandedProcessingPatternTerminal(ItemStack is) {
        super(is);
    }

    @Inject(method = "isCraftingRecipe", at = @At("HEAD"), cancellable = true)
    private void ae2qolextras$isCraftingRecipe(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(super.isCraftingRecipe());
    }

    @Inject(method = "setCraftingRecipe", at = @At("HEAD"), cancellable = true)
    private void ae2qolextras$setCraftingRecipe(boolean craftingMode, CallbackInfo ci) {
        super.setCraftingRecipe(craftingMode);
        ci.cancel();
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.setCraftingRecipe(data.getBoolean("craftingMode"));
        this.setSubstitution(data.getBoolean("substitute"));
        Ae2fcCompat.readExtras(this, data);
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("craftingMode", this.craftingMode);
        data.setBoolean("substitute", this.substitute);
        Ae2fcCompat.writeExtras(this, data);
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        if (!Ae2fcCompat.tryLoadFluidPattern(this, inv, slot, this.crafting, this.output)) {
            super.onChangeInventory(inv, slot, mc, removedStack, newStack);
        }

        // Crafting patterns only fill the first 9 slots; leftovers would sit hidden in the 3x3 view.
        if (inv == this.pattern && this.isCraftingRecipe()) {
            for (int x = CRAFTING_GRID_DIMENSION * CRAFTING_GRID_DIMENSION; x < this.crafting.getSlots(); x++) {
                if (!this.crafting.getStackInSlot(x).isEmpty()) {
                    this.crafting.setStackInSlot(x, ItemStack.EMPTY);
                }
            }
        }
    }
}
