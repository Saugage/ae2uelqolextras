package com.ae2qolextras.client;

import appeng.core.AppEng;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModel;
import com.ae2qolextras.Tags;
import com.ae2qolextras.part.ActivePlaneModels;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Supplies the connected-texture plane models, which AE2 builds in code rather than from JSON.
 */
public class ActivePlaneModelLoader implements ICustomModelLoader {

    private static final ResourceLocation FRONT_OFF = new ResourceLocation(Tags.MODID, "items/part/active_formation_plane");
    private static final ResourceLocation FRONT_ON = new ResourceLocation(Tags.MODID, "parts/active_formation_plane_on");
    private static final ResourceLocation SIDES = new ResourceLocation(AppEng.MOD_ID, "parts/plane_sides");
    private static final ResourceLocation BACK = new ResourceLocation(AppEng.MOD_ID, "parts/transition_plane_back");

    private final Map<String, IModel> models = new HashMap<>();

    public ActivePlaneModelLoader() {
        for (final PlaneConnections connections : PlaneConnections.PERMUTATIONS) {
            this.models.put(ActivePlaneModels.PREFIX_OFF + connections.getFilenameSuffix(), new PlaneModel(FRONT_OFF, SIDES, BACK, connections));
            this.models.put(ActivePlaneModels.PREFIX_ON + connections.getFilenameSuffix(), new PlaneModel(FRONT_ON, SIDES, BACK, connections));
        }
    }

    private static String stripModelsPrefix(final String path) {
        return path.startsWith("models/") ? path.substring("models/".length()) : path;
    }

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        return modelLocation.getNamespace().equals(Tags.MODID) && this.models.containsKey(stripModelsPrefix(modelLocation.getPath()));
    }

    @Override
    public IModel loadModel(final ResourceLocation modelLocation) {
        return this.models.get(stripModelsPrefix(modelLocation.getPath()));
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {}
}
