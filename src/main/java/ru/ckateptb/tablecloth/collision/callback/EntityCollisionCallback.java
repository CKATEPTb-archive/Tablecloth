package ru.ckateptb.tablecloth.collision.callback;

import org.bukkit.entity.Entity;

public interface EntityCollisionCallback extends CollisionCallback<Entity> {
    @Override
    boolean onCollision(Entity o);
}
