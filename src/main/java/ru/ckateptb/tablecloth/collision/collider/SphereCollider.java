package ru.ckateptb.tablecloth.collision.collider;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.AbstractCollider;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.ImmutableVector;

@Getter
public class SphereCollider extends AbstractCollider {
    public final ImmutableVector center;
    public final double radius;

    public SphereCollider(World world, double radius) {
        this(world, ImmutableVector.ZERO, radius);
    }

    public SphereCollider(World world, Vector center, double radius) {
        super(world);
        this.center = new ImmutableVector(center);
        this.radius = radius;
    }

    @Override
    public boolean intersects(Collider collider) {
        if (collider instanceof SphereCollider sphereCollider) {
            return intersects(sphereCollider);
        }
        if (collider instanceof AxisAlignedBoundingBoxCollider axisAlignedBoundingBoxCollider) {
            return intersects(axisAlignedBoundingBoxCollider);
        }
        if (collider instanceof RayCollider rayCollider) {
            return rayCollider.intersects(this);
        }
        if (collider instanceof OrientedBoundingBoxCollider orientedBoundingBoxCollider) {
            return intersects(orientedBoundingBoxCollider);
        }
        if (collider instanceof CompositeCollider compositeCollider) {
            return compositeCollider.intersects(this);
        }
        return false;
    }

    private boolean intersects(OrientedBoundingBoxCollider orientedBoundingBoxCollider) {
        ImmutableVector vector = center.subtract(orientedBoundingBoxCollider.closestPosition(center));
        return vector.dot(vector) <= radius * radius;
    }

    private boolean intersects(SphereCollider collider) {
        return collider.getWorld().equals(this.world) && this.center.distance(collider.center) <= this.radius + collider.radius;
    }

    public boolean intersects(AxisAlignedBoundingBoxCollider collider) {
        if (!collider.getWorld().equals(this.world)) return false;
        float dmin = 0;

        Vector min = collider.getMin();
        Vector max = collider.getMax();

        double centerX = center.getX();
        double minX = min.getX();
        double maxX = max.getX();
        if (centerX < minX) {
            dmin += Math.pow(centerX - minX, 2);
        } else if (centerX > maxX) {
            dmin += Math.pow(centerX - maxX, 2);
        }

        double centerY = center.getY();
        double minY = min.getY();
        double maxY = max.getY();
        if (centerY < minY) {
            dmin += Math.pow(centerY - minY, 2);
        } else if (centerY > maxY) {
            dmin += Math.pow(centerY - maxY, 2);
        }

        double centerZ = center.getZ();
        double minZ = min.getZ();
        double maxZ = max.getZ();
        if (centerZ < minZ) {
            dmin += Math.pow(centerZ - minZ, 2);
        } else if (centerZ > maxZ) {
            dmin += Math.pow(centerZ - maxZ, 2);
        }

        return dmin <= Math.pow(radius, 2);
    }

    @Override
    public ImmutableVector getPosition() {
        return center;
    }

    @Override
    public SphereCollider at(Vector point) {
        return new SphereCollider(this.world, point, radius);
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return new ImmutableVector(radius, radius, radius);
    }

    @Override
    public boolean contains(ImmutableVector point) {
        return point.isInSphere(center, radius);
    }
}
