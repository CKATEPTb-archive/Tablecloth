package ru.ckateptb.tablecloth.collision.callback;

public interface CollisionCallback<T> {
    boolean onCollision(T o);
}
