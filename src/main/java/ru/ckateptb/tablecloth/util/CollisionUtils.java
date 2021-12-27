package ru.ckateptb.tablecloth.util;

import org.apache.commons.math3.util.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.AABB;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.collision.Ray;

import java.util.Arrays;
import java.util.Optional;

public final class CollisionUtils {
    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean handleEntityCollisions(LivingEntity entity, Collider collider, Collider.CollisionCallback callback, boolean livingOnly) {
        return handleEntityCollisions(entity, collider, callback, livingOnly, false);
    }

    public static boolean handleEntityCollisions(LivingEntity entity, Collider collider, Collider.CollisionCallback callback, boolean livingOnly, boolean selfCollision) {
        return handleEntityCollisions(entity, collider, callback, livingOnly, false, selfCollision);
    }

    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean handleEntityCollisions(LivingEntity livingEntity, Collider collider, Collider.CollisionCallback callback, boolean livingOnly, boolean armorStandCollision, boolean selfCollision) {
        // This is used to increase the lookup volume for nearby entities.
        // Entity locations can be out of the collider volume while still intersecting.
        final double ExtentBuffer = 4.0;

        // Create the extent vector to use as size of bounding box to find nearby entities.
        Vector extent = collider.getHalfExtents().add(new Vector(ExtentBuffer, ExtentBuffer, ExtentBuffer));
        Vector pos = collider.getPosition();
        Location location = livingEntity.getLocation();
        location.setX(pos.getX());
        location.setY(pos.getY());
        location.setZ(pos.getZ());

        boolean hit = false;

        for (Entity entity : location.getWorld().getNearbyEntities(location, extent.getX(), extent.getY(), extent.getZ())) {
            if (!selfCollision && entity.equals(livingEntity)) continue;

            if (entity instanceof Player && ((Player) entity).getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            if (livingOnly && !(entity instanceof LivingEntity)) {
                continue;
            }

            if (entity instanceof ArmorStand armorStand && (!armorStandCollision || !armorStand.isVisible())) continue;

            AABB entityBounds = AABB.from(entity).at(entity.getLocation());
            if (collider.intersects(entityBounds)) {
                if (callback.onCollision(entity)) {
                    return true;
                }

                hit = true;
            }
        }

        return hit;
    }

    public static Pair<Boolean, Location> handleBlockCollisions(Collider collider, Location begin, Location end, boolean liquids) {
        if (end.equals(begin)) {
            return new Pair<>(false, null);
        }

        double maxExtent = VectorUtils.getMaxComponent(collider.getHalfExtents());
        double distance = begin.distance(end);

        Vector toEnd = end.subtract(begin).toVector().normalize();
        Ray ray = new Ray(begin.toVector(), toEnd);

        Location mid = begin.add(toEnd.multiply(distance / 2.0));
        double lookupRadius = (distance / 2.0) + maxExtent + 1.0;

        for (Block block : WorldUtils.getNearbyBlocks(mid, lookupRadius, Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR))) {
            AABB localBounds = AABB.from(block);

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

    public static Optional<Pair<Double, Double>> sweepAABB(AABB a, Vector aPrevPos, Vector aCurPos, AABB b, Vector bPrevPos, Vector bCurPos) {
        AABB aPrev = a.at(aPrevPos);
        AABB bPrev = b.at(bPrevPos);

        Vector da = aCurPos.subtract(aPrevPos);
        Vector db = bCurPos.subtract(bPrevPos);

        Vector v = db.subtract(da);

        Vector overlapFirst = new Vector(0, 0, 0);
        Vector overlapLast = new Vector(0, 0, 0);

        if (aPrev.intersects(bPrev)) {
            return Optional.of(new Pair<>(0.0, 0.0));
        }

        for (int i = 0; i < 3; ++i) {
            double aMax = VectorUtils.component(a.max(), i);
            double aMin = VectorUtils.component(a.min(), i);
            double bMax = VectorUtils.component(b.max(), i);
            double bMin = VectorUtils.component(b.min(), i);
            double vi = VectorUtils.component(v, i);

            if (aMax < bMin && vi < 0) {
                VectorUtils.setComponent(overlapFirst, i, (aMax - bMin) / vi);
            } else if (bMax < aMin && vi > 0) {
                VectorUtils.setComponent(overlapFirst, i, (aMin - bMax) / vi);
            } else if (bMax > aMin && vi < 0) {
                VectorUtils.setComponent(overlapLast, i, (aMin - bMax) / vi);
            } else if (aMax > bMin && vi > 0) {
                VectorUtils.setComponent(overlapLast, i, (aMax - bMin) / vi);
            }
        }

        double t1 = VectorUtils.getMaxComponent(overlapFirst);
        double t2 = VectorUtils.getMinComponent(overlapLast);

        if (t1 <= t2) {
            return Optional.of(new Pair<>(t1, t2));
        }

        return Optional.empty();
    }
}
