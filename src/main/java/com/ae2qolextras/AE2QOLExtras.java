package com.ae2qolextras;

import appeng.api.AEApi;
import com.ae2qolextras.block.ModBlocks;
import com.ae2qolextras.item.ModItems;
import com.ae2qolextras.part.ActivePlaneModels;
import com.ae2qolextras.spatial.SpatialLinkRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]",
     dependencies = "required-after:mixinbooter;required-after:appliedenergistics2")
public class AE2QOLExtras {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @Mod.Instance(Tags.MODID)
    public static AE2QOLExtras instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        AEApi.instance().registries().partModels().registerModels(ActivePlaneModels.getPlaneModelLocations());
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        ModBlocks.registerBlocks(event.getRegistry());
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        ModBlocks.registerItems(event.getRegistry());
        ModItems.registerItems(event.getRegistry());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        ModItems.registerUpgrades();
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        SpatialLinkRegistry.clear();
    }
}
