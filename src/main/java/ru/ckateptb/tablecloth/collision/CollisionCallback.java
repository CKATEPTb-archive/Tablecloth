package ru.ckateptb.tablecloth.collision;

import org.bukkit.entity.Entity;

public interface CollisionCallback {
    boolean onCollision(Entity e);
}
