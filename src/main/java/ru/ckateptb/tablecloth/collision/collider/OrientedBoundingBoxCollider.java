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

import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.AbstractCollider;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.ImmutableVector;
import ru.ckateptb.tablecloth.math.Rotation;

import java.util.Arrays;

public class OrientedBoundingBoxCollider extends AbstractCollider {
    private final ImmutableVector center;
    private final ImmutableVector[] axes;
    private final ImmutableVector halfExtents; // Half extents in local space.

    private OrientedBoundingBoxCollider(World world, Vector center, Vector[] axes, Vector halfExtents) {
        super(world);
        this.center = new ImmutableVector(center);
        this.axes = Arrays.stream(axes).map(ImmutableVector::new).toArray(ImmutableVector[]::new);
        this.halfExtents = new ImmutableVector(halfExtents);
    }

    public OrientedBoundingBoxCollider(AxisAlignedBoundingBoxCollider collider) {
        super(collider.getWorld());
        this.center = collider.getPosition();
        this.axes = new ImmutableVector[]{ImmutableVector.PLUS_I, ImmutableVector.PLUS_J, ImmutableVector.PLUS_K};
        this.halfExtents = collider.getHalfExtents();
    }

    public OrientedBoundingBoxCollider(AxisAlignedBoundingBoxCollider aabb, Rotation rotation) {
        super(aabb.getWorld());
        this.center = rotation.applyTo(aabb.getPosition());
        double[][] m = rotation.getMatrix();
        this.axes = new ImmutableVector[3];
        for (int i = 0; i < 3; i++) {
            this.axes[i] = new ImmutableVector(m[i]);
        }
        this.halfExtents = aabb.getHalfExtents();
    }

    public OrientedBoundingBoxCollider(AxisAlignedBoundingBoxCollider aabb, ImmutableVector axis, double angle) {
        this(aabb, new Rotation(axis, angle));
    }

    @Override
    public boolean intersects(Collider collider) {
        if(!collider.getWorld().equals(world)) {
            return false;
        }
        if (collider instanceof SphereCollider sphereCollider) {
            return sphereCollider.intersects(this);
        }
        if (collider instanceof AxisAlignedBoundingBoxCollider axisAlignedBoundingBoxCollider) {
            return intersects(new OrientedBoundingBoxCollider(axisAlignedBoundingBoxCollider));
        }
        if (collider instanceof OrientedBoundingBoxCollider orientedBoundingBoxCollider) {
            return intersects(orientedBoundingBoxCollider);
        }
        if (collider instanceof RayCollider rayCollider) {
            return rayCollider.intersects(this);
        }
        if (collider instanceof CompositeCollider compositeCollider) {
            return compositeCollider.intersects(this);
        }
        return false;
    }

    private boolean intersects(OrientedBoundingBoxCollider collider) {
        final ImmutableVector pos = collider.center.subtract(center);
        for (int i = 0; i < 3; i++) {
            if (getSeparatingPlane(pos, axes[i], collider) || getSeparatingPlane(pos, collider.axes[i], collider)) {
                return false;
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (getSeparatingPlane(pos, axes[i].crossProduct(collider.axes[j]), collider)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean getSeparatingPlane(ImmutableVector pos, ImmutableVector plane, OrientedBoundingBoxCollider collider) {
        final double dot = Math.abs(pos.dot(plane));
        final double x1 = Math.abs((axes[0].multiply(halfExtents.getX())).dot(plane));
        final double y1 = Math.abs((axes[1].multiply(halfExtents.getY())).dot(plane));
        final double z1 = Math.abs((axes[2].multiply(halfExtents.getZ())).dot(plane));
        final double x2 = Math.abs((collider.axes[0].multiply(collider.halfExtents.getX())).dot(plane));
        final double y2 = Math.abs((collider.axes[1].multiply(collider.halfExtents.getY())).dot(plane));
        final double z2 = Math.abs((collider.axes[2].multiply(collider.halfExtents.getZ())).dot(plane));
        return dot > x1 + y1 + z1 + x2 + y2 + z2;
    }

    // Returns the position closest to the target that lies on/in the OBB.
    public ImmutableVector closestPosition(ImmutableVector target) {
        ImmutableVector t = target.subtract(center);
        ImmutableVector closest = center;
        double[] extentArray = halfExtents.toArray();
        for (int i = 0; i < 3; i++) {
            ImmutableVector axis = axes[i];
            double r = extentArray[i];
            double dist = Math.max(-r, Math.min(t.dot(axis), r));
            closest = closest.add(axis.multiply(dist));
        }
        return closest;
    }

    @Override
    public ImmutableVector getPosition() {
        return center;
    }

    @Override
    public OrientedBoundingBoxCollider at(Vector point) {
        return new OrientedBoundingBoxCollider(world, point, axes, halfExtents);
    }

    @Override
    public ImmutableVector getHalfExtents() {
        double x = halfExtents.dot(ImmutableVector.PLUS_I);
        double y = halfExtents.dot(ImmutableVector.PLUS_J);
        double z = halfExtents.dot(ImmutableVector.PLUS_K);
        return new ImmutableVector(x, y, z);
    }

    @Override
    public boolean contains(ImmutableVector point) {
        return closestPosition(point).distanceSquared(point) <= 0.1;
    }
}
