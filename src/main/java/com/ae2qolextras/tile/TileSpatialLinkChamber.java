package com.ae2qolextras.tile;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.storage.ISpatialDimension;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import com.ae2qolextras.spatial.SpatialLinkRegistry;
import com.ae2qolextras.spatial.SpatialTeleporter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Holds a Spatial Storage Cell and offers this tile's grid node to Spatial Network Relays placed inside that cell.
 */
public class TileSpatialLinkChamber extends AENetworkInvTile implements ITickable {

    private static final int RECHECK_INTERVAL = 20;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1, 1, new SpatialCellFilter());

    private int boundCellId = SpatialLinkRegistry.NO_CELL;
    private boolean updatePending = false;
    private int ticks = 0;

    public TileSpatialLinkChamber() {
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
        this.getProxy().setIdlePowerUsage(2);
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Nonnull
    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
        this.updatePending = true;
    }

    @MENetworkEventSubscribe
    public void onPowerStatusChange(final MENetworkPowerStatusChange c) {
        this.updatePending = true;
    }

    @MENetworkEventSubscribe
    public void onChannelsChanged(final MENetworkChannelsChanged c) {
        this.updatePending = true;
    }

    @MENetworkEventSubscribe
    public void onBootingStatusChange(final MENetworkBootingStatusChange c) {
        this.updatePending = true;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.updatePending = true;
    }

    @Override
    public void update() {
        if (this.world.isRemote) {
            return;
        }

        if (this.updatePending || ++this.ticks >= RECHECK_INTERVAL) {
            this.updatePending = false;
            this.ticks = 0;
            this.updateBinding();
        }
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeInt(this.boundCellId);
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean changed = super.readFromStream(data);
        final int old = this.boundCellId;
        this.boundCellId = data.readInt();
        return changed || old != this.boundCellId;
    }

    private static int getCellId(final ItemStack is) {
        if (!is.isEmpty() && is.getItem() instanceof ISpatialStorageCell) {
            final ISpatialStorageCell cell = (ISpatialStorageCell) is.getItem();
            if (cell.isSpatialStorage(is)) {
                return cell.getStoredDimensionID(is);
            }
        }
        return SpatialLinkRegistry.NO_CELL;
    }

    private void updateBinding() {
        final IGridNode node = this.getProxy().getNode();
        if (node == null || !node.isActive()) {
            return;
        }

        final int newCellId = getCellId(this.inv.getStackInSlot(0));
        if (newCellId != this.boundCellId) {
            SpatialLinkRegistry.unbindHost(this.boundCellId, node);
            this.boundCellId = newCellId;
            SpatialLinkRegistry.bindHost(newCellId, node);
            this.markForUpdate();
        } else if (newCellId != SpatialLinkRegistry.NO_CELL && SpatialLinkRegistry.findHostNode(newCellId) != node) {
            // The node was recreated, or another chamber held the same cell before this one.
            SpatialLinkRegistry.bindHost(newCellId, node);
        }
    }

    private void unbind() {
        if (this.world != null && !this.world.isRemote && this.boundCellId != SpatialLinkRegistry.NO_CELL) {
            SpatialLinkRegistry.unbindHost(this.boundCellId, this.getProxy().getNode());
            this.boundCellId = SpatialLinkRegistry.NO_CELL;
        }
    }

    public boolean hasBoundStorage() {
        return this.boundCellId != SpatialLinkRegistry.NO_CELL;
    }

    public void teleportInside(final EntityPlayerMP player) {
        if (this.boundCellId == SpatialLinkRegistry.NO_CELL) {
            return;
        }

        final ItemStack is = this.inv.getStackInSlot(0);
        if (!(is.getItem() instanceof ISpatialStorageCell)) {
            return;
        }

        final ISpatialDimension spatial = ((ISpatialStorageCell) is.getItem()).getSpatialDimension();
        if (spatial == null || !(spatial.getWorld() instanceof WorldServer)) {
            return;
        }

        final BlockPos origin = spatial.getCellDimensionOrigin(this.boundCellId);
        if (origin == null) {
            return;
        }

        player.closeScreen();
        SpatialTeleporter.teleport(player, (WorldServer) spatial.getWorld(), origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
    }

    public void teleportOutside(final EntityPlayerMP player) {
        final World world = this.world;
        if (!(world instanceof WorldServer)) {
            return;
        }
        SpatialTeleporter.teleport(player, (WorldServer) world, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5);
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void invalidate() {
        this.unbind();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        this.unbind();
        super.onChunkUnload();
    }

    private static class SpatialCellFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(final IItemHandler inv, final int slot, final int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(final IItemHandler inv, final int slot, final ItemStack stack) {
            return stack.getItem() instanceof ISpatialStorageCell && ((ISpatialStorageCell) stack.getItem()).isSpatialStorage(stack);
        }
    }
}
