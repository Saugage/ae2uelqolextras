package com.ae2qolextras.part;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Iterator;

/**
 * Lets {@link appeng.helpers.MultiCraftingTracker} test whether the plane has room for a crafted item.
 */
final class PlaneInventoryAdaptor extends InventoryAdaptor {

    private final PartActiveFormationPlane plane;
    private final IActionSource src;

    PlaneInventoryAdaptor(final PartActiveFormationPlane plane, final IActionSource src) {
        this.plane = plane;
        this.src = src;
    }

    private ItemStack inject(final ItemStack stack, final Actionable mode) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final IAEItemStack leftover = this.plane.injectItems(AEItemStack.fromItemStack(stack), mode, this.src);
        return leftover == null ? ItemStack.EMPTY : leftover.createItemStack();
    }

    @Override
    public ItemStack addItems(final ItemStack toBeAdded) {
        return this.inject(toBeAdded, Actionable.MODULATE);
    }

    @Override
    public ItemStack simulateAdd(final ItemStack toBeSimulated) {
        return this.inject(toBeSimulated, Actionable.SIMULATE);
    }

    @Override
    public ItemStack removeItems(final int amount, final ItemStack filter, final IInventoryDestination destination) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateRemove(final int amount, final ItemStack filter, final IInventoryDestination destination) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeSimilarItems(final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateSimilarRemove(final int amount, final ItemStack filter, final FuzzyMode fuzzyMode, final IInventoryDestination destination) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean containsItems() {
        return false;
    }

    @Override
    public boolean hasSlots() {
        return true;
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return Collections.emptyIterator();
    }
}
