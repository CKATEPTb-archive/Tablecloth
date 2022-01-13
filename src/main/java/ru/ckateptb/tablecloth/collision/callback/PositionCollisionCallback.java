package ru.ckateptb.tablecloth.collision.callback;

import org.bukkit.util.Vector;

public interface PositionCollisionCallback extends CollisionCallback<Vector> {
    @Override
    CollisionCallbackResult onCollision(Vector object);
}
