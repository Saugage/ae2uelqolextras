package com.ae2qolextras.block;

import com.ae2qolextras.tile.TileSpatialNetworkRelay;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockSpatialNetworkRelay extends Block {

    public static final PropertyBool CONNECTED = PropertyBool.create("connected");

    public BlockSpatialNetworkRelay() {
        super(Material.IRON);
        this.setHardness(2.2F);
        this.setResistance(10.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CONNECTED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONNECTED);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getActualState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntity te = world.getTileEntity(pos);
        return state.withProperty(CONNECTED, te instanceof TileSpatialNetworkRelay && ((TileSpatialNetworkRelay) te).isConnected());
    }

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new TileSpatialNetworkRelay();
    }

    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSpatialNetworkRelay && placer instanceof EntityPlayer) {
            ((TileSpatialNetworkRelay) te).getProxy().setOwner((EntityPlayer) placer);
        }
    }
}
