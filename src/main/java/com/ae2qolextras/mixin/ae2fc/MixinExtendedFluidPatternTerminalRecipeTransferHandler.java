package com.ae2qolextras.mixin.ae2fc;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import com.ae2qolextras.AE2QOLExtras;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.integration.jei.ExtendedFluidPatternTerminalRecipeTransferHandler;
import com.glodblock.github.integration.jei.RecipeTransferBuilder;
import com.glodblock.github.interfaces.FCFluidPatternPart;
import com.glodblock.github.network.CPacketLoadPattern;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(value = ExtendedFluidPatternTerminalRecipeTransferHandler.class, remap = false)
public abstract class MixinExtendedFluidPatternTerminalRecipeTransferHandler {

    @Inject(
            method = "transferRecipe(Lcom/glodblock/github/client/container/ContainerExtendedFluidPatternTerminal;Lmezz/jei/api/gui/IRecipeLayout;Lnet/minecraft/entity/player/EntityPlayer;ZZ)Lmezz/jei/api/recipe/transfer/IRecipeTransferError;",
            at = @At("HEAD"),
            cancellable = true)
    private void ae2qolextras$transferRecipe(ContainerExtendedFluidPatternTerminal container, IRecipeLayout recipeLayout, EntityPlayer player,
                                             boolean maxTransfer, boolean doTransfer, CallbackInfoReturnable<IRecipeTransferError> cir) {
        cir.setReturnValue(null);

        if (!doTransfer || !(container.part instanceof FCFluidPatternPart)) {
            return;
        }

        final boolean craftingRecipe = recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING);
        if (container.isCraftingMode() != craftingRecipe) {
            try {
                NetworkHandler.instance().sendToServer(new PacketValueConfig("PatternTerminal.CraftMode", craftingRecipe ? "1" : "0"));
            } catch (final IOException e) {
                AE2QOLExtras.LOGGER.error("Failed to switch pattern terminal mode", e);
            }
        }

        final RecipeTransferBuilder transfer = new RecipeTransferBuilder(recipeLayout)
                .clearEmptySlot(!craftingRecipe)
                .putFluidFirst(container.fluidFirst)
                .build();
        // Combining would compact the inputs and break the 3x3 crafting layout.
        FluidCraft.proxy.netHandler.sendToServer(new CPacketLoadPattern(transfer.getInput(), transfer.getOutput(), container.combine && !craftingRecipe));
    }
}
