package com.ae2qolextras.compat;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import appeng.parts.reporting.AbstractPartEncoder;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemLargeEncodedPattern;
import com.glodblock.github.interfaces.FCFluidPatternPart;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import com.glodblock.github.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Restores the AE2 Fluid Crafting behaviour for the Extended Processing Pattern Terminal part that its own mixin
 * would add, since that mixin's methods are replaced by ours. ae2fc classes must only be touched inside {@link Impl}.
 */
public final class Ae2fcCompat {

    private static final boolean LOADED = Loader.isModLoaded("ae2fc");

    private Ae2fcCompat() {
    }

    public static void readExtras(AbstractPartEncoder part, NBTTagCompound data) {
        if (LOADED) {
            Impl.readExtras(part, data);
        }
    }

    public static void writeExtras(AbstractPartEncoder part, NBTTagCompound data) {
        if (LOADED) {
            Impl.writeExtras(part, data);
        }
    }

    /**
     * @return true if the change was an ae2fc pattern being placed in the encoded pattern slot and it has been loaded
     */
    public static boolean tryLoadFluidPattern(AbstractPartEncoder part, IItemHandler inv, int slot,
                                              IItemHandlerModifiable crafting, IItemHandlerModifiable output) {
        return LOADED && Impl.tryLoadFluidPattern(part, inv, slot, crafting, output);
    }

    private static final class Impl {

        private static final String COMBINE_MODE = "combineMode";
        private static final String FLUID_FIRST = "fluidFirst";

        static void readExtras(AbstractPartEncoder part, NBTTagCompound data) {
            if (part instanceof FCFluidPatternPart) {
                final FCFluidPatternPart fc = (FCFluidPatternPart) part;
                fc.setCombineMode(data.getBoolean(COMBINE_MODE));
                fc.setFluidPlaceMode(data.getBoolean(FLUID_FIRST));
            }
        }

        static void writeExtras(AbstractPartEncoder part, NBTTagCompound data) {
            if (part instanceof FCFluidPatternPart) {
                final FCFluidPatternPart fc = (FCFluidPatternPart) part;
                data.setBoolean(COMBINE_MODE, fc.getCombineMode());
                data.setBoolean(FLUID_FIRST, fc.getFluidPlaceMode());
            }
        }

        static boolean tryLoadFluidPattern(AbstractPartEncoder part, IItemHandler inv, int slot,
                                           IItemHandlerModifiable crafting, IItemHandlerModifiable output) {
            if (slot != 1 || inv != part.getInventoryByName("pattern")) {
                return false;
            }

            final ItemStack is = inv.getStackInSlot(1);
            if (is.isEmpty() || !(is.getItem() instanceof ItemFluidEncodedPattern
                    || is.getItem() instanceof ItemFluidCraftEncodedPattern
                    || is.getItem() instanceof ItemLargeEncodedPattern)) {
                return false;
            }

            final ItemEncodedPattern pattern = (ItemEncodedPattern) is.getItem();
            final ICraftingPatternDetails details = pattern.getPatternForItem(is, part.getHost().getTile().getWorld());
            if (details != null) {
                part.setCraftingRecipe(details.isCraftable());
                part.setSubstitution(details.canSubstitute());

                Util.clearItemInventory(crafting);
                Util.clearItemInventory(output);

                if (details instanceof FluidCraftingPatternDetails) {
                    Util.putPattern(part, ((FluidCraftingPatternDetails) details).getOriginInputs(), details.getOutputs());
                    part.setCraftingRecipe(true);
                } else {
                    Util.putPattern(part, details.getInputs(), details.getOutputs());
                }
            }
            part.getHost().markForSave();
            return true;
        }
    }
}
