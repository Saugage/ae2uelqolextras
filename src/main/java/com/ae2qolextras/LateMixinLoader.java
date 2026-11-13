package com.ae2qolextras;

import java.util.Arrays;
import java.util.List;

import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.ILateMixinLoader;

public class LateMixinLoader implements ILateMixinLoader {

    private static final String PATTERN_TERMINAL_CONFIG = "mixins.ae2qolextras.patternterminal.json";
    private static final String AE2FC_CONFIG = "mixins.ae2qolextras.ae2fc.json";

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("mixins.ae2qolextras.json", PATTERN_TERMINAL_CONFIG, AE2FC_CONFIG);
    }

    @Override
    public boolean shouldMixinConfigQueue(Context context) {
        if (PATTERN_TERMINAL_CONFIG.equals(context.mixinConfig())) {
            return ModConfig.expandedPatternTerminalTweaks();
        }
        if (AE2FC_CONFIG.equals(context.mixinConfig())) {
            return ModConfig.expandedPatternTerminalTweaks() && context.isModPresent("ae2fc");
        }
        return true;
    }
}
