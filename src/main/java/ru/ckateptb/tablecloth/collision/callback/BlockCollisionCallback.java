package ru.ckateptb.tablecloth.collision.callback;

import org.bukkit.block.Block;

public interface BlockCollisionCallback extends CollisionCallback<Block> {
    @Override
    boolean onCollision(Block o);
}
