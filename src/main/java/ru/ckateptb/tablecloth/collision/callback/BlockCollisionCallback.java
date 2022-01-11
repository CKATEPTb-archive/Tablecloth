package ru.ckateptb.tablecloth.collision.callback;

import org.bukkit.block.Block;

public interface BlockCollisionCallback extends CollisionCallback<Block> {
    @Override
    CollisionCallbackResult onCollision(Block object);
}
