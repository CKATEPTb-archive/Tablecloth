package ru.ckateptb.tablecloth.util;

import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.RayCaster;
import ru.ckateptb.tablecloth.collision.geometry.AABB;
import ru.ckateptb.tablecloth.collision.geometry.Ray;

import java.util.*;

@NoArgsConstructor
public final class WorldUtils {

    public static Collection<Block> getNearbyBlocks(Location location, double radius) {
        return getNearbyBlocks(location, radius, Collections.emptyList());
    }

    public static Collection<Block> getNearbyBlocks(Location location, double radius, Material... ignoreMaterials) {
        return getNearbyBlocks(location, radius, Arrays.asList(ignoreMaterials));
    }

    public static Collection<Block> getNearbyBlocks(Location location, double radius, List<Material> ignoreMaterials) {
        int r = (int) radius + 2;

        double originX = location.getX();
        double originY = location.getY();
        double originZ = location.getZ();

        List<Block> blocks = new ArrayList<>();
        Vector pos = location.toVector();

        for (double x = originX - r; x <= originX + r; ++x) {
            for (double y = originY - r; y <= originY + r; ++y) {
                for (double z = originZ - r; z <= originZ + r; ++z) {
                    if (pos.distanceSquared(new Vector(x, y, z)) <= radius * radius) {
                        Block block = location.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));

                        if (!ignoreMaterials.contains(block.getType())) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }

        return blocks;
    }

    public static LivingEntity getTargetEntity(LivingEntity user, int range) {
        Location eyeLocation = user.getEyeLocation();
        Ray ray = new Ray(eyeLocation, AdaptUtils.adapt(eyeLocation.getDirection()));

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : user.getWorld().getNearbyEntities(user.getLocation(), range, range, range)) {
            if (entity.equals(user)) continue;
            if (!(entity instanceof LivingEntity)) continue;
            if (entity instanceof ArmorStand) continue;

            AABB entityBounds = AABB.from(entity).at(entity.getLocation());

            Optional<Double> result = entityBounds.intersects(ray);
            if (result.isPresent()) {
                double dist = result.get();

                if (dist < closestDist) {
                    closest = (LivingEntity) entity;
                    closestDist = dist;
                }
            }
        }

        if (closestDist > range) {
            return null;
        }

        return closest;
    }


    public static boolean isOnGround(Entity entity) {
        final double epsilon = 0.01;

        Location location = entity.getLocation();
        AABB entityBounds = AABB.from(entity).at(location.clone().subtract(0, epsilon, 0));

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                Block checkBlock = location.clone().add(x, -epsilon, z).getBlock();
                if (checkBlock.getType() == Material.AIR) continue;

                AABB checkBounds = AABB.from(checkBlock).at(checkBlock.getLocation());

                if (entityBounds.intersects(checkBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static double distanceAboveGround(Entity entity) {
        return distanceAboveGround(entity, Collections.emptySet());
    }

    public static double distanceAboveGround(Entity entity, Material... materials) {
        return distanceAboveGround(entity, new HashSet<>(Arrays.asList(materials)));
    }

    public static double distanceAboveGround(Entity entity, Set<Material> groundMaterials) {
        Location location = entity.getLocation();
        Ray ray = new Ray(location, AdaptUtils.adapt(new Vector(0, -1, 0)));

        for (double y = location.getY() - 1; y >= 0; --y) {
            location.setY(y);

            Block block = location.getBlock();
            AABB checkBounds;

            if (groundMaterials.contains(block.getType())) {
                checkBounds = AABB.BLOCK_BOUNDS;
            } else {
                checkBounds = AABB.from(block);
            }

            checkBounds = checkBounds.at(block.getLocation());

            Optional<Double> rayHit = checkBounds.intersects(ray);

            if (rayHit.isPresent()) {
                return rayHit.get();
            }
        }

        return Double.MAX_VALUE;
    }

    public static boolean canView(LivingEntity user, Location location, double maxRange) {
        return canView(user, location.getBlock(), maxRange);
    }

    public static boolean canView(LivingEntity user, Block block, double maxRange) {
        if (!user.getWorld().equals(block.getWorld())) return false;

        Vector direction = block.getLocation().clone().subtract(user.getEyeLocation()).toVector().clone().normalize();
        Ray viewRay = new Ray(user.getEyeLocation(), AdaptUtils.adapt(direction));
        Block viewBlock = RayCaster.blockCast(user.getWorld(), viewRay, maxRange, block.getType() == Material.WATER);

        return block.equals(viewBlock);
    }
}

