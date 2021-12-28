package ru.ckateptb.tablecloth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.collision.callback.BlockCollisionCallback;
import ru.ckateptb.tablecloth.collision.callback.CollisionCallback;
import ru.ckateptb.tablecloth.collision.callback.EntityCollisionCallback;
import ru.ckateptb.tablecloth.collision.collider.AABB;
import ru.ckateptb.tablecloth.math.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollisionUtils {
    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean handleEntityCollisions(LivingEntity user, Collider collider, EntityCollisionCallback callback, boolean livingOnly) {
        return handleEntityCollisions(user, collider, callback, livingOnly, false);
    }

    public static boolean handleEntityCollisions(LivingEntity user, Collider collider, EntityCollisionCallback callback, boolean livingOnly, boolean selfCollision) {
        return handleEntityCollisions(user, collider, callback, livingOnly, selfCollision, false);
    }

    private static Predicate<Entity> entityPredicate(Entity source, boolean livingOnly, boolean selfCollision, boolean armorStandCollision) {
        Predicate<Entity> livingPredicate = livingOnly ? e -> e instanceof LivingEntity : e -> true;
        Predicate<Entity> selfPredicate = !selfCollision ? e -> !e.equals(source) : e -> true;
        Predicate<Entity> valid = e -> isValidEntity(e, armorStandCollision);
        return selfPredicate.and(livingPredicate).and(valid);
    }

    private static boolean isValidEntity(Entity entity, boolean armorStandCollision) {
        if (entity instanceof Player player) {
            return player.getGameMode() != GameMode.SPECTATOR;
        } else if (entity instanceof ArmorStand armorStand) {
            return armorStandCollision || armorStand.isVisible();
        }
        return true;
    }

    public static boolean handleEntityCollisions(LivingEntity user, Collider collider, EntityCollisionCallback callback, boolean livingOnly, boolean selfCollision, boolean armorStandCollision) {
        Vector3d extent = collider.getHalfExtents();
        Vector3d pos = collider.getPosition();
        boolean hit = false;
        Predicate<Entity> filter = entityPredicate(user, livingOnly, selfCollision, armorStandCollision);
        for (Entity entity : user.getWorld().getNearbyEntities(pos.toLocation(user.getWorld()), extent.getX(), extent.getY(), extent.getZ(), filter)) {
            if (collider.intersects(AABB.from(entity).at(new Vector3d(entity.getLocation())))) {
                boolean result = callback.onCollision(entity);
                hit |= result;
            }
        }
        return hit;
    }


    public static List<Block> handleBlockCollisions(LivingEntity user, Collider collider, BlockCollisionCallback callback, Predicate<Block> blockPredicate, boolean ignoreLiquids) {
        double maxExtent = Vector3d.maxComponent(collider.getHalfExtents());
        Location center = collider.getPosition().toLocation(user.getWorld());
        List<Block> blocks = new ArrayList<>();
        for (Block block : WorldUtils.getNearbyBlocks(center, maxExtent, blockPredicate)) {
            AABB localBounds = AABB.from(block);
            if (block.isLiquid() && !ignoreLiquids) {
                localBounds = AABB.BLOCK_BOUNDS;
            }

            AABB blockBounds = localBounds.at(new Vector3d(block.getLocation()));

            if (!blocks.contains(block) && blockBounds.intersects(collider)) {
                blocks.add(block);
                if(callback.onCollision(block)) return blocks;
            }
        }
        return blocks;
    }
}
