package ru.ckateptb.tablecloth.collision;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.ckateptb.tablecloth.collision.geometry.AABB;
import ru.ckateptb.tablecloth.collision.geometry.Ray;
import ru.ckateptb.tablecloth.collision.vector.VectorUtils;
import ru.ckateptb.tablecloth.util.AdaptUtils;
import ru.ckateptb.tablecloth.util.WorldUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollisionUtil {
    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean handleEntityCollisions(LivingEntity user, Collider collider, CollisionCallback callback, boolean livingOnly) {
        return handleEntityCollisions(user, collider, callback, livingOnly, false);
    }

    public static boolean handleEntityCollisions(LivingEntity user, Collider collider, CollisionCallback callback, boolean livingOnly, boolean selfCollision) {
        return handleEntityCollisions(user, collider, callback, livingOnly, selfCollision, false);
    }

    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean handleEntityCollisions(LivingEntity user, Collider collider, CollisionCallback callback, boolean livingOnly, boolean selfCollision, boolean armorStandCollision) {
        // This is used to increase the lookup volume for nearby entities.
        // Entity locations can be out of the collider volume while still intersecting.
        final double ExtentBuffer = 4.0;

        // Create the extent vector to use as size of bounding box to find nearby entities.
        Vector3D extent = collider.getHalfExtents().add(new Vector3D(ExtentBuffer, ExtentBuffer, ExtentBuffer));
        Vector3D pos = collider.getPosition();
        Location location = user.getLocation();
        location.setX(pos.getX());
        location.setY(pos.getY());
        location.setZ(pos.getZ());

        boolean hit = false;

        for (Entity entity : Objects.requireNonNull(location.getWorld()).getNearbyEntities(location, extent.getX(), extent.getY(), extent.getZ())) {
            if (!selfCollision && entity.equals(user)) continue;

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

        Vector3D toEnd = AdaptUtils.adapt(end.clone().subtract(begin).toVector()).normalize();
        Ray ray = new Ray(AdaptUtils.adapt(begin.toVector()), toEnd);

        Location mid = begin.clone().add(AdaptUtils.adapt(toEnd.scalarMultiply(distance / 2.0)));
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
                    return new Pair<>(true, begin.clone().add(AdaptUtils.adapt(toEnd.scalarMultiply(d))));
                }
            }
        }

        return new Pair<>(false, null);
    }

    public static Optional<Pair<Double, Double>> sweepAABB(AABB a, Vector3D aPrevPos, Vector3D aCurPos, AABB b, Vector3D bPrevPos, Vector3D bCurPos) {
        AABB aPrev = a.at(aPrevPos);
        AABB bPrev = b.at(bPrevPos);

        Vector3D da = aCurPos.subtract(aPrevPos);
        Vector3D db = bCurPos.subtract(bPrevPos);

        Vector3D v = db.subtract(da);

        Vector3D overlapFirst = Vector3D.ZERO;
        Vector3D overlapLast = Vector3D.ZERO;

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
