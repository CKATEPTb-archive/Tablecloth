package ru.ckateptb.tablecloth.collision;

import ru.ckateptb.tablecloth.math.Vector3d;

public interface Collider {
    boolean intersects(Collider collider);

    Vector3d getPosition();

    Collider at(Vector3d point);

    Vector3d getHalfExtents();

    boolean contains(Vector3d point);
}
