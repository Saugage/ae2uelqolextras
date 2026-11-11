package com.ae2qolextras.item;

import appeng.api.config.Upgrades;
import appeng.core.CreativeTab;
import com.ae2qolextras.Tags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModItems {

    public static Item activeFormationPlane;

    private ModItems() {}

    public static void registerItems(final IForgeRegistry<Item> registry) {
        activeFormationPlane = setup(new ItemPartActiveFormationPlane(), "active_formation_plane");
        registry.register(activeFormationPlane);
    }

    public static void registerUpgrades() {
        final ItemStack plane = new ItemStack(activeFormationPlane);
        Upgrades.CAPACITY.registerItem(plane, 5);
        Upgrades.SPEED.registerItem(plane, 4);
        Upgrades.FUZZY.registerItem(plane, 1);
        Upgrades.INVERTER.registerItem(plane, 1);
        Upgrades.CRAFTING.registerItem(plane, 1);
        Upgrades.REDSTONE.registerItem(plane, 1);
    }

    private static Item setup(final Item item, final String name) {
        item.setRegistryName(Tags.MODID, name);
        item.setTranslationKey(Tags.MODID + "." + name);
        if (CreativeTab.instance != null) {
            item.setCreativeTab(CreativeTab.instance);
        }
        return item;
    }
}
