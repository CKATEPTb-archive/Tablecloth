package ru.ckateptb.tablecloth.collision.callback;

import org.bukkit.entity.Entity;

public interface EntityCollisionCallback extends CollisionCallback<Entity> {
    @Override
    CollisionCallbackResult onCollision(Entity entity);
}
