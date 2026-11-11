package com.ae2qolextras.part;

import appeng.api.parts.IPartModel;
import appeng.parts.PartModel;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModels;
import com.ae2qolextras.Tags;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AE2's {@link PlaneModels} only resolves models in its own namespace, so the active plane keeps its own lookup.
 */
public final class ActivePlaneModels {

    public static final String PREFIX_OFF = "part/active_formation_plane_";
    public static final String PREFIX_ON = "part/active_formation_plane_on_";

    private static final Map<PlaneConnections, IPartModel> MODELS_OFF = new HashMap<>();
    private static final Map<PlaneConnections, IPartModel> MODELS_ON = new HashMap<>();
    private static final Map<PlaneConnections, IPartModel> MODELS_HAS_CHANNEL = new HashMap<>();

    static {
        for (final PlaneConnections connections : PlaneConnections.PERMUTATIONS) {
            final ResourceLocation planeOff = new ResourceLocation(Tags.MODID, PREFIX_OFF + connections.getFilenameSuffix());
            final ResourceLocation planeOn = new ResourceLocation(Tags.MODID, PREFIX_ON + connections.getFilenameSuffix());
            MODELS_OFF.put(connections, new PartModel(PlaneModels.MODEL_CHASSIS_OFF, planeOff));
            MODELS_ON.put(connections, new PartModel(PlaneModels.MODEL_CHASSIS_ON, planeOff));
            MODELS_HAS_CHANNEL.put(connections, new PartModel(PlaneModels.MODEL_CHASSIS_HAS_CHANNEL, planeOn));
        }
    }

    private ActivePlaneModels() {}

    public static IPartModel getModel(final PlaneConnections connections, final boolean hasPower, final boolean hasChannel) {
        if (hasPower && hasChannel) {
            return MODELS_HAS_CHANNEL.get(connections);
        } else if (hasPower) {
            return MODELS_ON.get(connections);
        }
        return MODELS_OFF.get(connections);
    }

    public static List<ResourceLocation> getPlaneModelLocations() {
        final List<ResourceLocation> locations = new ArrayList<>();
        for (final PlaneConnections connections : PlaneConnections.PERMUTATIONS) {
            locations.add(new ResourceLocation(Tags.MODID, PREFIX_OFF + connections.getFilenameSuffix()));
            locations.add(new ResourceLocation(Tags.MODID, PREFIX_ON + connections.getFilenameSuffix()));
        }
        return locations;
    }
}
