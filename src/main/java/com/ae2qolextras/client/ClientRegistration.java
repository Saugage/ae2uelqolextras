package com.ae2qolextras.client;

import com.ae2qolextras.Tags;
import com.ae2qolextras.block.ModBlocks;
import com.ae2qolextras.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public final class ClientRegistration {

    private ClientRegistration() {}

    @SubscribeEvent
    public static void registerModels(final ModelRegistryEvent event) {
        for (final Block block : new Block[] {ModBlocks.spatialLinkChamber, ModBlocks.spatialNetworkRelay}) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
        ModelLoader.setCustomModelResourceLocation(ModItems.activeFormationPlane, 0,
                new ModelResourceLocation(ModItems.activeFormationPlane.getRegistryName(), "inventory"));
        ModelLoaderRegistry.registerLoader(new ActivePlaneModelLoader());
    }
}
