package ru.ckateptb.tablecloth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.ckateptb.tablecloth.collision.callback.CollisionCallbackResult;
import ru.ckateptb.tablecloth.collision.collider.AxisAlignedBoundingBoxCollider;
import ru.ckateptb.tablecloth.math.ImmutableVector;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorldUtils {
    public static boolean isOnGround(Entity entity) {
        if (!(entity instanceof Player)) {
            return entity.isOnGround();
        }
        AxisAlignedBoundingBoxCollider entityBounds = new AxisAlignedBoundingBoxCollider(entity).grow(new ImmutableVector(0, 0.05, 0));
        AxisAlignedBoundingBoxCollider floorBounds = new AxisAlignedBoundingBoxCollider(entity.getWorld(), new ImmutableVector(-1, -0.1, -1), new ImmutableVector(1, 0.1, 1)).at(new ImmutableVector(entity.getLocation()));
        AtomicBoolean result = new AtomicBoolean(false);
        floorBounds.handleBlockCollisions(block -> {
            result.set(entityBounds.intersects(new AxisAlignedBoundingBoxCollider(block).at(new ImmutableVector(block))));
            return result.get() ? CollisionCallbackResult.END : CollisionCallbackResult.CONTINUE;
        });
        return result.get();
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

