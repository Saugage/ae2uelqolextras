package com.ae2qolextras.tile;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.spatial.StorageWorldProvider;
import appeng.tile.grid.AENetworkTile;
import com.ae2qolextras.spatial.SpatialLinkRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ITickable;

import java.io.IOException;

/**
 * Placed inside a spatial cell, joins the AE grid of the Spatial Link Chamber that currently holds that cell by
 * creating a grid connection to the chamber's node.
 */
public class TileSpatialNetworkRelay extends AENetworkTile implements ITickable {

    private static final int RETRY_INTERVAL = 40;

    private IGridConnection link;
    private boolean updatePending = false;
    private boolean clientConnected = false;
    private int ticks = 0;

    public TileSpatialNetworkRelay() {
        this.getProxy().setFlags(GridFlags.DENSE_CAPACITY);
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    private boolean isInStorageWorld() {
        return this.world != null && this.world.provider instanceof StorageWorldProvider;
    }

    @Override
    public void onReady() {
        super.onReady();
        if (!this.world.isRemote && this.isInStorageWorld()) {
            SpatialLinkRegistry.registerRelay(this);
            this.updatePending = true;
        }
    }

    public void requestStatusUpdate() {
        this.updatePending = true;
    }

    @Override
    public void update() {
        if (this.world.isRemote || !this.isInStorageWorld()) {
            return;
        }

        if (this.updatePending || (this.link == null && ++this.ticks >= RETRY_INTERVAL)) {
            this.updatePending = false;
            this.ticks = 0;
            this.updateStatus();
        }
    }

    private void updateStatus() {
        final int cellId = SpatialLinkRegistry.findCellAt(this.world, this.pos);
        final IGridNode host = SpatialLinkRegistry.findHostNode(cellId);
        final IGridNode node = this.getProxy().getNode();
        if (host == null || node == null) {
            this.destroyLink();
            return;
        }

        if (this.link != null && node.getConnections().contains(this.link)) {
            final IGridNode a = this.link.a();
            final IGridNode b = this.link.b();
            if ((a == node && b == host) || (a == host && b == node)) {
                return;
            }
        }

        this.destroyLink();

        try {
            this.link = AEApi.instance().grid().createGridConnection(node, host);
            this.markForUpdate();
        } catch (final FailedConnectionException ignored) {
            // Retried on the next interval.
        }
    }

    private void destroyLink() {
        if (this.link != null) {
            // Destroying a node already tears down its connections, and destroying one twice breaks the grid.
            final IGridNode node = this.getProxy().getNode();
            if (node != null && node.getConnections().contains(this.link)) {
                this.link.destroy();
            }
            this.link = null;
            this.markForUpdate();
        }
    }

    public boolean isConnected() {
        return this.world != null && this.world.isRemote ? this.clientConnected : this.link != null;
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.link != null);
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean changed = super.readFromStream(data);
        final boolean old = this.clientConnected;
        this.clientConnected = data.readBoolean();
        return changed || old != this.clientConnected;
    }

    private void detach() {
        if (this.world != null && !this.world.isRemote) {
            this.destroyLink();
            SpatialLinkRegistry.unregisterRelay(this);
        }
    }

    @Override
    public void invalidate() {
        this.detach();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        this.detach();
        super.onChunkUnload();
    }
}
