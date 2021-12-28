/*
 * Copyright 2020-2021 Moros
 *
 * This file is part of Bending.
 *
 * Bending is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bending is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bending. If not, see <https://www.gnu.org/licenses/>.
 */

package ru.ckateptb.tablecloth.collision.collider;

import org.bukkit.Location;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.Rotation;
import ru.ckateptb.tablecloth.math.Vector3d;

import static java.lang.Math.abs;

/**
 * Oriented bounding box
 */
public class OBB implements Collider {
    private final Vector3d center;
    private final Vector3d[] axes;
    private final Vector3d e; // Half extents in local space.

    private OBB(Vector3d center, Vector3d[] axes, Vector3d halfExtents) {
        this.center = center;
        this.axes = new Vector3d[3];
        System.arraycopy(axes, 0, this.axes, 0, 3);
        this.e = halfExtents;
    }

    public OBB(AABB aabb) {
        this.center = aabb.getPosition();
        this.axes = new Vector3d[]{Vector3d.PLUS_I, Vector3d.PLUS_J, Vector3d.PLUS_K};
        this.e = aabb.getHalfExtents();
    }

    public OBB(AABB aabb, Rotation rotation) {
        this.center = rotation.applyTo(aabb.getPosition());
        double[][] m = rotation.getMatrix();
        this.axes = new Vector3d[3];
        for (int i = 0; i < 3; i++) {
            this.axes[i] = new Vector3d(m[i]);
        }
        this.e = aabb.getHalfExtents();
    }

    public OBB(AABB aabb, Vector3d axis, double angle) {
        this(aabb, new Rotation(axis, angle));
    }

    public OBB addPosition(Vector3d position) {
        return new OBB(center.add(position), this.axes, e);
    }

    public OBB addPosition(Location location) {
        return addPosition(new Vector3d(location.toVector()));
    }

    @Override
    public boolean intersects(Collider collider) {
        if (collider instanceof DummyCollider) {
            return false;
        } else if (collider instanceof Sphere) {
            return collider.intersects(this);
        } else if (collider instanceof AABB aabb) {
            return intersects(new OBB(aabb));
        } else if (collider instanceof OBB obb) {
            return intersects(obb);
        } else if (collider instanceof Disc) {
            return collider.intersects(this);
        }
        return false;
    }

    private boolean intersects(OBB other) {
        final Vector3d pos = other.center.subtract(center);
        for (int i = 0; i < 3; i++) {
            if (getSeparatingPlane(pos, axes[i], other) || getSeparatingPlane(pos, other.axes[i], other)) {
                return false;
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (getSeparatingPlane(pos, axes[i].cross(other.axes[j]), other)) {
                    return false;
                }
            }
        }
        return true;
    }

    // check if there's a separating plane in between the selected axes
    private boolean getSeparatingPlane(Vector3d pos, Vector3d plane, OBB other) {
        final double dot = abs(pos.dot(plane));
        final double x1 = abs((axes[0].multiply(e.getX())).dot(plane));
        final double y1 = abs((axes[1].multiply(e.getY())).dot(plane));
        final double z1 = abs((axes[2].multiply(e.getZ())).dot(plane));
        final double x2 = abs((other.axes[0].multiply(other.e.getX())).dot(plane));
        final double y2 = abs((other.axes[1].multiply(other.e.getY())).dot(plane));
        final double z2 = abs((other.axes[2].multiply(other.e.getZ())).dot(plane));
        return dot > x1 + y1 + z1 + x2 + y2 + z2;
    }

    // Returns the position closest to the target that lies on/in the OBB.
    public Vector3d closestPosition(Vector3d target) {
        Vector3d t = target.subtract(center);
        Vector3d closest = center;
        double[] extentArray = e.toArray();
        for (int i = 0; i < 3; i++) {
            Vector3d axis = axes[i];
            double r = extentArray[i];
            double dist = Math.max(-r, Math.min(t.dot(axis), r));
            closest = closest.add(axis.multiply(dist));
        }
        return closest;
    }

    @Override
    public Vector3d getPosition() {
        return center;
    }

    @Override
    public OBB at(Vector3d point) {
        return new OBB(point, axes, e);
    }

    @Override
    public Vector3d getHalfExtents() {
        double x = e.dot(Vector3d.PLUS_I);
        double y = e.dot(Vector3d.PLUS_J);
        double z = e.dot(Vector3d.PLUS_K);
        return new Vector3d(x, y, z);
    }

    @Override
    public boolean contains(Vector3d point) {
        return closestPosition(point).distanceSq(point) <= 0.01;
    }
}
