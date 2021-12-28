package ru.ckateptb.tablecloth.collision;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import ru.ckateptb.tablecloth.collision.geometry.AABB;
import ru.ckateptb.tablecloth.collision.geometry.Ray;
import ru.ckateptb.tablecloth.collision.vector.VectorUtils;
import ru.ckateptb.tablecloth.util.AdaptUtils;

import java.util.*;

public final class RayCaster {
    private static final List<Vector3D> DIRECTIONS = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K,
            Vector3D.MINUS_I, Vector3D.MINUS_J, Vector3D.MINUS_K,

            new Vector3D(0, 1, 1), new Vector3D(0, 1, -1),
            new Vector3D(1, 1, 0), new Vector3D(1, 1, 1), new Vector3D(1, 1, -1),
            new Vector3D(-1, 1, 0), new Vector3D(-1, 1, 1), new Vector3D(-1, 1, -1),

            new Vector3D(0, -1, 1), new Vector3D(0, -1, -1),
            new Vector3D(1, -1, 0), new Vector3D(1, -1, 1), new Vector3D(1, -1, -1),
            new Vector3D(-1, -1, 0), new Vector3D(-1, -1, 1), new Vector3D(-1, -1, -1)
    );

    private RayCaster() {

    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T entityCast(LivingEntity user, Ray ray, double maxRange, double scale, Class<? extends T> type) {
        // Cast a ray out to find the farthest location. Don't select entities through blocks.
        Location maxLocation = cast(user, ray, maxRange, false, false);
        int radius = (int) maxLocation.distance(user.getEyeLocation());
        Location start = new Location(user.getWorld(), ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        double closestDistance = Double.MAX_VALUE;
        Entity closest = null;

        for (Entity entity : user.getWorld().getNearbyEntities(start, radius, radius, radius)) {
            if (entity.equals(user)) continue;
            if (!type.isAssignableFrom(entity.getClass())) continue;

            AABB entityBounds = AABB.from(entity).scale(scale).at(entity.getLocation());
            Optional<Double> result = entityBounds.intersects(ray);
            if (result.isPresent()) {
                double distance = result.get();

                if (distance < closestDistance && distance >= 0) {
                    closestDistance = distance;
                    closest = entity;
                }
            }
        }

        return (T) closest;
    }

    public static Location cast(LivingEntity user, Ray ray, double maxRange, boolean liquidCollision, boolean entityCollision) {
        World world = user.getWorld();
        Location location = cast(world, ray, maxRange, liquidCollision);

        if (!entityCollision) {
            return location;
        }

        int radius = (int) maxRange + 3;
        Location start = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        double closestDistance = location.clone().subtract(start).length();

        for (Entity entity : world.getNearbyEntities(start, radius, radius, radius)) {
            if (entity.equals(user)) continue;
            if (!(entity instanceof LivingEntity)) continue;

            AABB entityBounds = AABB.from(entity).at(entity.getLocation());

            Optional<Double> result = entityBounds.intersects(ray);
            if (result.isPresent()) {
                double distance = result.get();
                distance += VectorUtils.getMaxComponent(AABB.from(entity).getHalfExtents());

                if (distance < closestDistance && distance >= 0) {
                    closestDistance = distance;
                }
            }
        }

        return start.clone().add(AdaptUtils.adapt(ray.direction.scalarMultiply(closestDistance)));
    }

    public static Location cast(LivingEntity user, Ray ray, double maxRange, boolean liquidCollision, boolean entityCollision, double selectRadius, List<Block> ignoreBlocks) {
        World world = user.getWorld();
        Location location = cast(world, ray, maxRange, liquidCollision, ignoreBlocks);

        if (!entityCollision) {
            return location;
        }

        int radius = (int) maxRange + 3;
        Location start = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        double closestDistance = location.clone().subtract(start).length();
        Location bestLocation = null;

        for (Entity entity : world.getNearbyEntities(start, radius, radius, radius)) {
            if (entity.equals(user)) continue;
            if (!(entity instanceof LivingEntity)) continue;

            AABB entityBounds = AABB.from(entity)
                    .scale(selectRadius)
                    .at(entity.getLocation().add(0, AABB.from(entity).getHalfExtents().getY(), 0));

            Optional<Double> result = entityBounds.intersects(ray);
            if (result.isPresent()) {
                Location hit = ((LivingEntity) entity).getEyeLocation();
                double distance = hit.toVector().distance(AdaptUtils.adapt(ray.origin)) + 1.0;

                if (distance < closestDistance && distance >= 0) {
                    closestDistance = distance;
                    bestLocation = hit;
                }
            }
        }

        if (bestLocation != null) {
            return bestLocation;
        }

        return start.clone().add(AdaptUtils.adapt(ray.direction.scalarMultiply(closestDistance)));
    }

    public static Location cast(World world, Ray ray, double maxRange, boolean liquidCollision) {
        return cast(world, ray, maxRange, liquidCollision, Collections.emptyList());
    }

    public static Location cast(World world, Ray ray, double maxRange, boolean liquidCollision, List<Block> ignoreBlocks) {
        Location origin = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        double closestDistance = Double.MAX_VALUE;

        // Progress through each block and check all neighbors for ray intersection.
        for (double i = 0; i < maxRange + 1; ++i) {
            Location current = origin.clone().add(AdaptUtils.adapt(ray.direction.scalarMultiply(i)));
            for (Vector3D direction : DIRECTIONS) {
                Location check = current.clone().add(AdaptUtils.adapt(direction));
                Block block = check.getBlock();

                if (ignoreBlocks.contains(block)) {
                    continue;
                }

                AABB localBounds = AABB.from(block);

                if (liquidCollision && block.isLiquid()) {
                    localBounds = AABB.BLOCK_BOUNDS;
                }

                AABB blockBounds = localBounds.at(block.getLocation());

                Optional<Double> result = blockBounds.intersects(ray);
                if (result.isPresent()) {
                    double distance = result.get();
                    if (distance < closestDistance && distance >= 0) {
                        closestDistance = distance;
                    }
                }
            }

            // Break early after checking all neighbors for intersection.
            if (closestDistance < maxRange) {
                break;
            }
        }

        closestDistance = Math.min(closestDistance, maxRange);
        return origin.clone().add(AdaptUtils.adapt(ray.direction.scalarMultiply(closestDistance)));
    }

    public static Block blockCast(World world, Ray ray, double maxRange, boolean liquidCollision) {
        if (liquidCollision) {
            return blockCast(world, ray, maxRange, Material.WATER);
        } else {
            return blockCast(world, ray, maxRange);
        }
    }

    public static Block blockCast(World world, Ray ray, double maxRange, List<Material> solids) {
        return blockCast(world, ray, maxRange, solids.toArray(new Material[0]));
    }

    public static Block blockCast(World world, Ray ray, double maxRange, Material... solids) {
        Location origin = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        double closestDistance = Double.MAX_VALUE;
        Block closestBlock = null;

        // Progress through each block and check all neighbors for ray intersection.
        for (double i = 0; i < maxRange + 1; ++i) {
            Location current = origin.clone().add(AdaptUtils.adapt(ray.direction.scalarMultiply(i)));
            for (Vector3D direction : DIRECTIONS) {
                Location check = current.clone().add(AdaptUtils.adapt(direction));
                Block block = check.getBlock();
                AABB localBounds = AABB.from(block);

                if (Arrays.asList(solids).contains(block.getType())) {
                    localBounds = AABB.BLOCK_BOUNDS;
                }

                AABB blockBounds = localBounds.at(block.getLocation());

                Optional<Double> result = blockBounds.intersects(ray);
                if (result.isPresent()) {
                    double distance = result.get();
                    if (distance < closestDistance && distance >= 0) {
                        closestDistance = distance;
                        closestBlock = block;
                    }
                }
            }

            // Break early after checking all neighbors for intersection.
            if (closestDistance < maxRange) {
                break;
            }
        }

        return closestBlock;
    }

    public static Block blockCastIgnore(World world, Ray ray, double maxRange, boolean liquidCollision, List<Block> ignoreBlocks) {
        Location origin = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        Block originBlock = origin.getBlock();
        double closestDistance = Double.MAX_VALUE;
        Block closestBlock = null;

        // Progress through each block and check all neighbors for ray intersection.
        for (double i = 0; i < maxRange + 1; ++i) {
            Location current = origin.clone().add(AdaptUtils.adapt(ray.direction.scalarMultiply(i)));

            for (Vector3D direction : DIRECTIONS) {
                Location check = current.clone().add(AdaptUtils.adapt(direction));
                Block block = check.getBlock();
                AABB localBounds = AABB.from(block);

                if (ignoreBlocks.contains(block)) continue;

                if (liquidCollision && block.getType() == Material.WATER) {
                    localBounds = AABB.BLOCK_BOUNDS;
                }

                AABB blockBounds = localBounds.at(block.getLocation());

                Optional<Double> result = blockBounds.intersects(ray);
                if (result.isPresent()) {
                    double distance = result.get();

                    if (distance < closestDistance && (distance >= 0 || block.equals(originBlock))) {
                        closestDistance = distance;
                        closestBlock = block;
                    }
                }
            }

            // Break early after checking all neighbors for intersection.
            if (closestDistance < maxRange) {
                break;
            }
        }

        return closestBlock;
    }

    // Casts a ray and returns every block that intersects that ray.
    public static List<Block> blockArray(World world, Ray ray, double range) {
        List<Block> blocks = new ArrayList<>();
        Location origin = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());

        // Progress through each block and check all neighbors for ray intersection.
        for (double i = 0; i < range + 1; ++i) {
            Location current = origin.clone().add(AdaptUtils.adapt(ray.direction.scalarMultiply(i)));
            for (Vector3D direction : DIRECTIONS) {
                Block block = current.clone().add(AdaptUtils.adapt(direction)).getBlock();
                AABB blockBounds = AABB.BLOCK_BOUNDS.at(block.getLocation());

                Optional<Double> result = blockBounds.intersects(ray);
                if (result.isPresent()) {
                    double distance = result.get();

                    if (distance < range && distance >= 0) {
                        if (!blocks.contains(block)) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }

        return blocks;
    }
}
