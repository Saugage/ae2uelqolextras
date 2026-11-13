package com.ae2qolextras;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

/**
 * Read directly from the config file because mixin configs are chosen before Forge's {@code @Config} system loads.
 */
public final class ModConfig {

    private static Boolean expandedPatternTerminalTweaks;

    private ModConfig() {
    }

    public static boolean expandedPatternTerminalTweaks() {
        if (expandedPatternTerminalTweaks == null) {
            load();
        }
        return expandedPatternTerminalTweaks;
    }

    private static void load() {
        final Configuration config = new Configuration(new File(Loader.instance().getConfigDir(), Tags.MODID + ".cfg"));
        final Property prop = config.get(Configuration.CATEGORY_GENERAL, "expandedPatternTerminalTweaks", true,
                "Enables crafting pattern encoding in the Expanded Processing Pattern Terminal, along with its "
                        + "AE2 Fluid Crafting compatibility.");
        prop.setRequiresMcRestart(true);
        expandedPatternTerminalTweaks = prop.getBoolean();
        if (config.hasChanged()) {
            config.save();
        }
    }
}
