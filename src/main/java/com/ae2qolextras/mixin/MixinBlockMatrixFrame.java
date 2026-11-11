package com.ae2qolextras.mixin;

import appeng.block.AEBaseBlock;
import appeng.block.spatial.BlockMatrixFrame;
import appeng.spatial.StorageWorldProvider;
import com.ae2qolextras.spatial.SpatialLinkRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

// Makes the walls around spatial cells targetable so blocks can be placed against them, and lets players sneak-click
// them to return to the Spatial Link Chamber holding the cell.
@Mixin(BlockMatrixFrame.class)
public abstract class MixinBlockMatrixFrame extends AEBaseBlock {

    private MixinBlockMatrixFrame(Material mat) {
        super(mat);
    }

    @Inject(method = "getSelectedBoundingBoxesFromPool", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2qolextras$selectableFullCube(World w, BlockPos pos, Entity thePlayer, boolean b, CallbackInfoReturnable<Iterable<AxisAlignedBB>> cir) {
        cir.setReturnValue(Collections.singletonList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)));
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player.isSneaking() && world.provider instanceof StorageWorldProvider && player instanceof EntityPlayerMP) {
            SpatialLinkRegistry.teleportPlayerOut((EntityPlayerMP) player, SpatialLinkRegistry.findCellAt(world, pos));
        }
        return false;
    }
}
