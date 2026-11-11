package com.ae2qolextras.mixin;

import appeng.container.implementations.ContainerPatternEncoder;
import appeng.tile.inventory.AppEngInternalInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ContainerPatternEncoder.class, remap = false)
public interface ContainerPatternEncoderAccessor {

    @Accessor("cOut")
    AppEngInternalInventory ae2qolextras$getCOut();
}
