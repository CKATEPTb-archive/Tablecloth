package ru.ckateptb.tablecloth.collision.callback;

public interface CollisionCallback<T> {
    CollisionCallbackResult onCollision(T object);
}
