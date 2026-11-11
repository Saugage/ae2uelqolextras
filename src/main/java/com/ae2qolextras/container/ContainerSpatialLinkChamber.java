package com.ae2qolextras.container;

import com.ae2qolextras.tile.TileSpatialLinkChamber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSpatialLinkChamber extends Container {

    public static final int BUTTON_TELEPORT_INSIDE = 0;

    private static final int CELL_SLOTS = 1;
    private static final int PLAYER_INV_Y = 84;

    private final TileSpatialLinkChamber tile;

    public ContainerSpatialLinkChamber(final InventoryPlayer playerInv, final TileSpatialLinkChamber tile) {
        this.tile = tile;

        this.addSlotToContainer(new SlotItemHandler(tile.getInternalInventory(), 0, 80, 37));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, PLAYER_INV_Y + 58));
        }
    }

    public TileSpatialLinkChamber getTile() {
        return this.tile;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        return !this.tile.isInvalid() && this.tile.getWorld().getTileEntity(this.tile.getPos()) == this.tile
                && player.getDistanceSq(this.tile.getPos().getX() + 0.5, this.tile.getPos().getY() + 0.5, this.tile.getPos().getZ() + 0.5) <= 64.0D;
    }

    @Override
    public boolean enchantItem(final EntityPlayer player, final int id) {
        if (id == BUTTON_TELEPORT_INSIDE && player instanceof EntityPlayerMP) {
            this.tile.teleportInside((EntityPlayerMP) player);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int index) {
        final Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        final ItemStack stack = slot.getStack();
        final ItemStack original = stack.copy();

        if (index < CELL_SLOTS) {
            if (!this.mergeItemStack(stack, CELL_SLOTS, this.inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.inventorySlots.get(0).isItemValid(stack) || !this.mergeItemStack(stack, 0, CELL_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return original;
    }
}
