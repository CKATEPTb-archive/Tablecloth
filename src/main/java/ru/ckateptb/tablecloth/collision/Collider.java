package ru.ckateptb.tablecloth.collision;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.World;

public interface Collider {
    boolean intersects(Collider collider);

    Vector3D getPosition();

    Vector3D getHalfExtents();

    World getWorld();

    boolean contains(Vector3D point);
}
