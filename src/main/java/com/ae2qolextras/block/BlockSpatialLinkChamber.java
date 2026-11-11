package com.ae2qolextras.block;

import com.ae2qolextras.AE2QOLExtras;
import com.ae2qolextras.GuiHandler;
import com.ae2qolextras.tile.TileSpatialLinkChamber;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockSpatialLinkChamber extends Block {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool BOUND = PropertyBool.create("bound");

    public BlockSpatialLinkChamber() {
        super(Material.IRON);
        this.setHardness(2.2F);
        this.setResistance(10.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(BOUND, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, BOUND);
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntity te = world.getTileEntity(pos);
        return state.withProperty(BOUND, te instanceof TileSpatialLinkChamber && ((TileSpatialLinkChamber) te).hasBoundStorage());
    }

    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing facing, final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer, final EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new TileSpatialLinkChamber();
    }

    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSpatialLinkChamber && placer instanceof EntityPlayer) {
            ((TileSpatialLinkChamber) te).getProxy().setOwner((EntityPlayer) placer);
        }
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            if (!player.getHeldItemMainhand().isEmpty() || !player.getHeldItemOffhand().isEmpty()) {
                return false;
            }
            final TileEntity tile = world.getTileEntity(pos);
            if (!world.isRemote && tile instanceof TileSpatialLinkChamber && player instanceof EntityPlayerMP) {
                ((TileSpatialLinkChamber) tile).teleportInside((EntityPlayerMP) player);
            }
            return true;
        }
        if (!(world.getTileEntity(pos) instanceof TileSpatialLinkChamber)) {
            return false;
        }
        if (!world.isRemote) {
            player.openGui(AE2QOLExtras.instance, GuiHandler.SPATIAL_LINK_CHAMBER, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSpatialLinkChamber) {
            final List<ItemStack> drops = new ArrayList<>();
            ((TileSpatialLinkChamber) te).getDrops(world, pos, drops);
            for (final ItemStack drop : drops) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
        }
        super.breakBlock(world, pos, state);
    }
}
