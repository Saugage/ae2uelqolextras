package com.ae2qolextras.item;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import com.ae2qolextras.part.PartActiveFormationPlane;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemPartActiveFormationPlane extends Item implements IPartItem<PartActiveFormationPlane> {

    @Override
    public PartActiveFormationPlane createPartFromItemStack(final ItemStack is) {
        return new PartActiveFormationPlane(is);
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side,
                                      final float hitX, final float hitY, final float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }
}
