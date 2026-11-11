package com.ae2qolextras.spatial;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;

import java.util.ArrayList;
import java.util.List;

public class SpatialTeleporter implements ITeleporter {

    private final double x;
    private final double y;
    private final double z;

    private SpatialTeleporter(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void placeEntity(final World world, final Entity entity, final float yaw) {
        entity.setLocationAndAngles(this.x, this.y, this.z, yaw, entity.rotationPitch);
        entity.motionX = entity.motionY = entity.motionZ = 0.0D;
    }

    /**
     * Moves an entity, and whatever it rides or is ridden by, to another dimension. Mirrors AE2's private
     * StorageHelper.teleportEntity.
     */
    public static Entity teleport(final Entity entity, final WorldServer target, final double x, final double y, final double z) {
        if (target == null || !(entity.world instanceof WorldServer)) {
            return entity;
        }

        // A chamber can sit inside another spatial cell, which is the same storage dimension.
        if (entity.world == target) {
            entity.dismountRidingEntity();
            entity.setPositionAndUpdate(x, y, z);
            return entity;
        }

        if (entity.isRiding()) {
            return teleport(entity.getRidingEntity(), target, x, y, z);
        }

        final List<Entity> passengers = new ArrayList<>();
        for (final Entity passenger : entity.getPassengers()) {
            passenger.dismountRidingEntity();
            passengers.add(teleport(passenger, target, x, y, z));
        }

        target.getChunkProvider().provideChunk(MathHelper.floor(x) >> 4, MathHelper.floor(z) >> 4);

        final Entity moved = entity.changeDimension(target.provider.getDimension(), new SpatialTeleporter(x, y, z));
        final Entity result = moved != null ? moved : entity;

        for (final Entity passenger : passengers) {
            passenger.startRiding(result, true);
        }

        return result;
    }
}
