package com.ae2qolextras.spatial;

import appeng.api.networking.IGridNode;
import appeng.api.storage.ISpatialDimension;
import appeng.capabilities.Capabilities;
import com.ae2qolextras.tile.TileSpatialLinkChamber;
import com.ae2qolextras.tile.TileSpatialNetworkRelay;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Pairs Spatial Link Chambers with the spatial cell they hold, and lets Spatial Network Relays find the chamber of the
 * cell they are placed in.
 * <p>
 * AE2-UEL stores every spatial cell as a region of a single storage dimension, so everything is keyed by the cell's
 * storage ID rather than by dimension.
 */
public final class SpatialLinkRegistry {

    public static final int NO_CELL = -1;

    private static final Map<Integer, IGridNode> CELL_TO_HOST = new HashMap<>();
    private static final Set<TileSpatialNetworkRelay> RELAYS = Collections.newSetFromMap(new WeakHashMap<>());

    private SpatialLinkRegistry() {}

    public static void registerRelay(final TileSpatialNetworkRelay relay) {
        RELAYS.add(relay);
    }

    public static void unregisterRelay(final TileSpatialNetworkRelay relay) {
        RELAYS.remove(relay);
    }

    public static void bindHost(final int cellId, final IGridNode node) {
        if (cellId == NO_CELL || node == null) {
            return;
        }
        CELL_TO_HOST.put(cellId, node);
        updateRelays();
    }

    public static void unbindHost(final int cellId, final IGridNode node) {
        if (cellId == NO_CELL) {
            return;
        }
        // Another chamber may have taken over the same cell ID in the meantime.
        if (CELL_TO_HOST.get(cellId) == node) {
            CELL_TO_HOST.remove(cellId);
            updateRelays();
        }
    }

    public static @Nullable IGridNode findHostNode(final int cellId) {
        return CELL_TO_HOST.get(cellId);
    }

    /**
     * Returns the bound cell whose region (including the surrounding matrix frame shell) contains the position.
     */
    public static int findCellAt(final World storageWorld, final BlockPos pos) {
        final ISpatialDimension spatial = storageWorld.getCapability(Capabilities.SPATIAL_DIMENSION, null);
        if (spatial == null) {
            return NO_CELL;
        }

        for (final int cellId : CELL_TO_HOST.keySet()) {
            final BlockPos origin = spatial.getCellDimensionOrigin(cellId);
            final BlockPos size = spatial.getCellContentSize(cellId);
            if (origin == null || size == null) {
                continue;
            }
            if (pos.getX() >= origin.getX() - 1 && pos.getX() <= origin.getX() + size.getX()
                    && pos.getY() >= origin.getY() - 1 && pos.getY() <= origin.getY() + size.getY()
                    && pos.getZ() >= origin.getZ() - 1 && pos.getZ() <= origin.getZ() + size.getZ()) {
                return cellId;
            }
        }
        return NO_CELL;
    }

    public static void teleportPlayerOut(final EntityPlayerMP player, final int cellId) {
        final IGridNode host = findHostNode(cellId);
        if (host != null && host.getMachine() instanceof TileSpatialLinkChamber) {
            ((TileSpatialLinkChamber) host.getMachine()).teleportOutside(player);
        }
    }

    public static void clear() {
        CELL_TO_HOST.clear();
        RELAYS.clear();
    }

    private static void updateRelays() {
        for (final TileSpatialNetworkRelay relay : RELAYS.toArray(new TileSpatialNetworkRelay[0])) {
            if (relay.isInvalid()) {
                RELAYS.remove(relay);
            } else {
                relay.requestStatusUpdate();
            }
        }
    }
}
