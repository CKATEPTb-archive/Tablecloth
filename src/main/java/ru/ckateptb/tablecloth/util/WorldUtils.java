package ru.ckateptb.tablecloth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.ckateptb.tablecloth.collision.callback.CollisionCallbackResult;
import ru.ckateptb.tablecloth.collision.collider.AxisAlignedBoundingBoxCollider;
import ru.ckateptb.tablecloth.math.ImmutableVector;

import java.util.concurrent.atomic.AtomicBoolean;

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
}

