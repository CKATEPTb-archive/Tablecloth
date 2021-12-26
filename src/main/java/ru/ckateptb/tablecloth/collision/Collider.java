package ru.ckateptb.tablecloth.collision;

import com.mojang.datafixers.util.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.util.VectorUtils;
import ru.ckateptb.tablecloth.util.WorldUtils;

import java.util.Arrays;
import java.util.Optional;

public interface Collider {
    boolean intersects(Collider collider);

    Vector getPosition();

    Vector getHalfExtents();

    World getWorld();

    boolean contains(Vector point);

    default boolean handleEntityCollisions(CollisionCallback callback, Location location, boolean livingOnly) {
        return handleEntityCollisions(callback, location, livingOnly, false);
    }

    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    default boolean handleEntityCollisions(CollisionCallback callback, Location location, boolean livingOnly, boolean armorStandCollision) {
        // This is used to increase the lookup volume for nearby entities.
        // Entity locations can be out of the collider volume while still intersecting.
        final double ExtentBuffer = 4.0;

        // Create the extent vector to use as size of bounding box to find nearby entities.
        Vector extent = getHalfExtents().add(new Vector(ExtentBuffer, ExtentBuffer, ExtentBuffer));
        Vector pos = getPosition();
        location.setX(pos.getX());
        location.setY(pos.getY());
        location.setZ(pos.getZ());

        boolean hit = false;

        for (Entity entity : location.getWorld().getNearbyEntities(location, extent.getX(), extent.getY(), extent.getZ())) {
            if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            if (livingOnly && !(entity instanceof LivingEntity)) {
                continue;
            }

            if (entity instanceof ArmorStand) {
                if (!armorStandCollision || !((ArmorStand) entity).isVisible()) continue;
            }

            AABB entityBounds = BukkitAABB.getEntityBounds(entity).at(entity.getLocation());
            if (intersects(entityBounds)) {
                if (callback.onCollision(entity)) {
                    return true;
                }

                hit = true;
            }
        }

        return hit;
    }

    default Pair<Boolean, Location> handleBlockCollisions(Location begin, Location end, boolean liquids) {
        if (end.equals(begin)) {
            return new Pair<>(false, null);
        }

        double maxExtent = VectorUtils.getMaxComponent(getHalfExtents());
        double distance = begin.distance(end);

        Vector toEnd = end.subtract(begin).toVector().normalize();
        Ray ray = new Ray(begin.toVector(), toEnd);

        Location mid = begin.add(toEnd.multiply(distance / 2.0));
        double lookupRadius = (distance / 2.0) + maxExtent + 1.0;

        for (Block block : WorldUtils.getNearbyBlocks(mid, lookupRadius, Arrays.asList(Material.AIR, Material.AIR))) {
            AABB localBounds = BukkitAABB.getBlockBounds(block);

            if (liquids && block.isLiquid()) {
                localBounds = AABB.BLOCK_BOUNDS;
            }

            AABB blockBounds = localBounds.at(block.getLocation());

            Optional<Double> result = blockBounds.intersects(ray);
            if (result.isPresent()) {
                double d = result.get();
                if (d < distance) {
                    return new Pair<>(true, begin.add(toEnd.multiply(d)));
                }
            }
        }

        return new Pair<>(false, null);
    }

    interface CollisionCallback {
        boolean onCollision(Entity e);
    }
}

