package com.ae2qolextras;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import com.ae2qolextras.client.GuiActiveFormationPlane;
import com.ae2qolextras.client.GuiSpatialLinkChamber;
import com.ae2qolextras.container.ContainerActiveFormationPlane;
import com.ae2qolextras.container.ContainerSpatialLinkChamber;
import com.ae2qolextras.part.PartActiveFormationPlane;
import com.ae2qolextras.tile.TileSpatialLinkChamber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int SPATIAL_LINK_CHAMBER = 0;
    public static final int ACTIVE_FORMATION_PLANE = 1;

    /**
     * Part GUIs carry the part's side in the low 3 bits of the GUI id.
     */
    public static int partGuiId(final int gui, final AEPartLocation side) {
        return gui << 3 | side.ordinal();
    }

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        final int gui = id >> 3;
        if (gui == SPATIAL_LINK_CHAMBER && te instanceof TileSpatialLinkChamber) {
            return new ContainerSpatialLinkChamber(player.inventory, (TileSpatialLinkChamber) te);
        }
        final IPart part = getPart(te, id);
        if (gui == ACTIVE_FORMATION_PLANE && part instanceof PartActiveFormationPlane) {
            return new ContainerActiveFormationPlane(player.inventory, (PartActiveFormationPlane) part);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        final TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        final int gui = id >> 3;
        if (gui == SPATIAL_LINK_CHAMBER && te instanceof TileSpatialLinkChamber) {
            return new GuiSpatialLinkChamber(new ContainerSpatialLinkChamber(player.inventory, (TileSpatialLinkChamber) te));
        }
        final IPart part = getPart(te, id);
        if (gui == ACTIVE_FORMATION_PLANE && part instanceof PartActiveFormationPlane) {
            return new GuiActiveFormationPlane(new ContainerActiveFormationPlane(player.inventory, (PartActiveFormationPlane) part));
        }
        return null;
    }

    private static IPart getPart(final TileEntity te, final int id) {
        if (te instanceof IPartHost) {
            return ((IPartHost) te).getPart(AEPartLocation.fromOrdinal(id & 7));
        }
        return null;
    }
}
