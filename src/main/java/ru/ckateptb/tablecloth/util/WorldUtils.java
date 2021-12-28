package ru.ckateptb.tablecloth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.collision.RayTrace;
import ru.ckateptb.tablecloth.collision.collider.AABB;
import ru.ckateptb.tablecloth.collision.collider.DummyCollider;
import ru.ckateptb.tablecloth.math.FastMath;
import ru.ckateptb.tablecloth.math.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorldUtils {

    /**
     * @return {@link #getNearbyBlocks(Location, double, Predicate, int)} with predicate being always true and no block limit.
     */
    public static List<Block> getNearbyBlocks(Location location, double radius) {
        return getNearbyBlocks(location, radius, block -> true, 0);
    }

    /**
     * @return {@link #getNearbyBlocks(Location, double, Predicate, int)} with the given predicate and no block limit.
     */
    public static List<Block> getNearbyBlocks(Location location, double radius, Predicate<Block> predicate) {
        return getNearbyBlocks(location, radius, predicate, 0);
    }

    /**
     * Collects all blocks in a sphere that satisfy the given predicate.
     * <p> Note: Limit is only respected if positive. Otherwise all blocks that satisfy the given predicate are collected.
     *
     * @param location  the center point
     * @param radius    the radius of the sphere
     * @param predicate the predicate that needs to be satisfied for every block
     * @param limit     the amount of blocks to collect
     * @return all collected blocks
     */
    public static List<Block> getNearbyBlocks(Location location, double radius, Predicate<Block> predicate, int limit) {
        int r = FastMath.ceil(radius) + 1;
        double originX = location.getX();
        double originY = location.getY();
        double originZ = location.getZ();
        Vector3d pos = new Vector3d(location);
        List<Block> blocks = new ArrayList<>();
        for (double x = originX - r; x <= originX + r; x++) {
            for (double y = originY - r; y <= originY + r; y++) {
                for (double z = originZ - r; z <= originZ + r; z++) {
                    Vector3d loc = new Vector3d(x, y, z);
                    if (pos.distanceSq(loc) > radius * radius) {
                        continue;
                    }
                    Block block = loc.toBlock(location.getWorld());
                    if (predicate.test(block)) {
                        blocks.add(block);
                        if (limit > 0 && blocks.size() >= limit) {
                            return blocks;
                        }
                    }
                }
            }
        }
        return blocks;
    }

    /**
     * @return {@link #getNearbyBlocks(World, AABB, Predicate, int)} with predicate being always true and no block limit.
     */
    public static List<Block> getNearbyBlocks(World world, AABB box) {
        return getNearbyBlocks(world, box, block -> true, 0);
    }

    /**
     * @return {@link #getNearbyBlocks(World, AABB, Predicate, int)} with the given predicate and no block limit.
     */
    public static List<Block> getNearbyBlocks(World world, AABB box, Predicate<Block> predicate) {
        return getNearbyBlocks(world, box, predicate, 0);
    }

    /**
     * Collects all blocks inside a bounding box that satisfy the given predicate.
     * <p> Note: Limit is only respected if positive. Otherwise all blocks that satisfy the given predicate are collected.
     *
     * @param world     the world to check
     * @param box       the bounding box to check
     * @param predicate the predicate that needs to be satisfied for every block
     * @param limit     the amount of blocks to collect
     * @return all collected blocks
     */
    public static List<Block> getNearbyBlocks(World world, AABB box, Predicate<Block> predicate, int limit) {
        if (box == DummyCollider.INSTANCE) {
            return List.of();
        }
        List<Block> blocks = new ArrayList<>();
        for (double x = box.min.getX(); x <= box.max.getX(); x++) {
            for (double y = box.min.getY(); y <= box.max.getY(); y++) {
                for (double z = box.min.getZ(); z <= box.max.getZ(); z++) {
                    Vector3d loc = new Vector3d(x, y, z);
                    Block block = loc.toBlock(world);
                    if (predicate.test(block)) {
                        blocks.add(block);
                        if (limit > 0 && blocks.size() >= limit) {
                            return blocks;
                        }
                    }
                }
            }
        }
        return blocks;
    }

    public static boolean isOnGround(Entity entity) {
        if (!(entity instanceof Player)) {
            return entity.isOnGround();
        }
        AABB entityBounds = AABB.from(entity).grow(new Vector3d(0, 0.05, 0));
        AABB floorBounds = new AABB(new Vector3d(-1, -0.1, -1), new Vector3d(1, 0.1, 1)).at(new Vector3d(entity.getLocation()));
        for (Block block : getNearbyBlocks(entity.getWorld(), floorBounds, b -> !b.isPassable())) {
            if (entityBounds.intersects(AABB.from(block))) {
                return true;
            }
        }
        return false;
    }

    public static double getDistanceAboveGround(Entity entity) {
        int minHeight = entity.getWorld().getMinHeight();
        int deltaHeight = entity.getWorld().getMaxHeight() - minHeight;
        AABB entityBounds = AABB.from(entity).grow(new Vector3d(0, deltaHeight, 0));
        Block origin = entity.getLocation().getBlock();
        for (int i = 0; i < deltaHeight; i++) {
            Block check = origin.getRelative(BlockFace.DOWN, i);
            if (check.getY() <= minHeight) {
                break;
            }
            AABB checkBounds = check.isLiquid() ? AABB.BLOCK_BOUNDS.at(new Vector3d(check)) : AABB.from(check);
            if (checkBounds.intersects(entityBounds)) {
                return Math.max(0, entity.getBoundingBox().getMinY() - checkBounds.max.getY());
            }
        }
        return deltaHeight;
    }


    public static Vector3d getEntityCenter(Entity entity) {
        return new Vector3d(entity.getLocation()).add(new Vector3d(0, entity.getHeight() / 2, 0));
    }

    public static boolean canView(LivingEntity user, Location location, double maxRange) {
        return canView(user, location.getBlock(), maxRange);
    }

    public static boolean canView(LivingEntity user, Block block, double maxRange) {
        if (!user.getWorld().equals(block.getWorld())) return false;
        Block viewBlock = RayTrace.of(user).range(maxRange).ignoreLiquids(true).result(user.getWorld()).block();
        return block.equals(viewBlock);
    }

    public static Optional<Block> findTopBlock(Block block, int height, Predicate<Block> predicate) {
        for (int i = 1; i <= height; i++) {
            Block check = block.getRelative(BlockFace.UP, i);
            if (!predicate.test(check)) {
                return Optional.of(check.getRelative(BlockFace.DOWN));
            }
        }
        return Optional.empty();
    }

    public static Optional<Block> findBottomBlock(Block block, int height, Predicate<Block> predicate) {
        for (int i = 1; i <= height; i++) {
            Block check = block.getRelative(BlockFace.DOWN, i);
            if (!predicate.test(check)) {
                return Optional.of(check.getRelative(BlockFace.UP));
            }
        }
        return Optional.empty();
    }
}

