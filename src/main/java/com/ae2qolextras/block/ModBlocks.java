package com.ae2qolextras.block;

import appeng.core.CreativeTab;
import appeng.core.features.IStackSrc;
import appeng.tile.AEBaseTile;
import com.ae2qolextras.Tags;
import com.ae2qolextras.tile.TileSpatialLinkChamber;
import com.ae2qolextras.tile.TileSpatialNetworkRelay;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModBlocks {

    public static Block spatialLinkChamber;
    public static Block spatialNetworkRelay;

    private ModBlocks() {}

    public static void registerBlocks(final IForgeRegistry<Block> registry) {
        spatialLinkChamber = setup(new BlockSpatialLinkChamber(), "spatial_link_chamber");
        spatialNetworkRelay = setup(new BlockSpatialNetworkRelay(), "spatial_network_relay");
        registry.register(spatialLinkChamber);
        registry.register(spatialNetworkRelay);

        registerTile(TileSpatialLinkChamber.class, "spatial_link_chamber", spatialLinkChamber);
        registerTile(TileSpatialNetworkRelay.class, "spatial_network_relay", spatialNetworkRelay);
    }

    public static void registerItems(final IForgeRegistry<Item> registry) {
        for (final Block block : new Block[] {spatialLinkChamber, spatialNetworkRelay}) {
            registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
    }

    private static Block setup(final Block block, final String name) {
        block.setRegistryName(Tags.MODID, name);
        block.setTranslationKey(Tags.MODID + "." + name);
        if (CreativeTab.instance != null) {
            block.setCreativeTab(CreativeTab.instance);
        }
        return block;
    }

    private static void registerTile(final Class<? extends TileEntity> tile, final String name, final Block block) {
        GameRegistry.registerTileEntity(tile, new ResourceLocation(Tags.MODID, name));
        // Gives the grid node an item to show in AE2's network status screen.
        AEBaseTile.registerTileItem(tile, new IStackSrc() {
            @Override
            public ItemStack stack(final int i) {
                return new ItemStack(block, i);
            }

            @Override
            public Item getItem() {
                return Item.getItemFromBlock(block);
            }

            @Override
            public int getDamage() {
                return 0;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        });
    }
}
