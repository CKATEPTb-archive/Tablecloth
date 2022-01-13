package ru.ckateptb.tablecloth.collision.collider;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.AbstractCollider;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.ImmutableVector;

@Getter
public class AxisAlignedBoundingBoxCollider extends AbstractCollider {
    private final ImmutableVector min;
    private final ImmutableVector max;
    private final ImmutableVector position;

    public AxisAlignedBoundingBoxCollider(Entity entity) {
        super(entity.getWorld());
        BoundingBox boundingBox = entity.getBoundingBox();
        boundingBox.shift(entity.getLocation().toVector().multiply(-1));
        this.min = new ImmutableVector(boundingBox.getMin());
        this.max = new ImmutableVector(boundingBox.getMax());
        this.position = new ImmutableVector(entity.getLocation());
    }

    public AxisAlignedBoundingBoxCollider(Block block) {
        super(block.getWorld());
        boolean liquid = block.isLiquid();
        BoundingBox boundingBox = block.getBoundingBox();
        boundingBox.shift(block.getLocation().toVector().multiply(-1));
        this.min = liquid ? ImmutableVector.ZERO : new ImmutableVector(boundingBox.getMin());
        this.max = liquid ? ImmutableVector.ONE : new ImmutableVector(boundingBox.getMax());
        this.position = new ImmutableVector(block.getLocation());
    }

    public AxisAlignedBoundingBoxCollider(World world, Vector min, Vector max) {
        this(world, min, max, new Vector(0, 0, 0));
    }

    public AxisAlignedBoundingBoxCollider(World world, Vector min, Vector max, Vector position) {
        super(world);
        this.min = new ImmutableVector(min);
        this.max = new ImmutableVector(max);
        this.position = new ImmutableVector(position);
    }

    @Override
    public boolean intersects(Collider collider) {
        if (!collider.getWorld().equals(world)) {
            return false;
        }
        if (collider instanceof AxisAlignedBoundingBoxCollider axisAlignedBoundingBoxCollider) {
            return intersects(axisAlignedBoundingBoxCollider);
        }
        if (collider instanceof SphereCollider sphereCollider) {
            return sphereCollider.intersects(this);
        }
        if (collider instanceof RayCollider rayCollider) {
            return rayCollider.intersects(this);
        }
        if (collider instanceof OrientedBoundingBoxCollider orientedBoundingBoxCollider) {
            return orientedBoundingBoxCollider.intersects(this);
        }
        if (collider instanceof CompositeCollider compositeCollider) {
            return compositeCollider.intersects(this);
        }
        return false;
    }

    private boolean intersects(AxisAlignedBoundingBoxCollider collider) {
        return this.toBoundingBox().overlaps(collider.toBoundingBox());
    }

    @Override
    public ImmutableVector getPosition() {
        return position;
    }

    public ImmutableVector getCenter() {
        return getMin().add(getMax().subtract(getMin()).multiply(0.5));
    }

    @Override
    public AxisAlignedBoundingBoxCollider at(Vector point) {
        return new AxisAlignedBoundingBoxCollider(world, min, max, point);
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return max.subtract(min).multiply(0.5).abs();
    }

    @Override
    public boolean contains(ImmutableVector point) {
        return point.isInAABB(this);
    }

    public ImmutableVector getMin() {
        return min.add(position);
    }

    public ImmutableVector getMax() {
        return max.add(position);
    }

    public BoundingBox toBoundingBox() {
        return BoundingBox.of(this.getMin(), this.getMax());
    }

    public AxisAlignedBoundingBoxCollider grow(double x, double y, double z) {
        return grow(new ImmutableVector(x, y, z));
    }

    public AxisAlignedBoundingBoxCollider scale(double x, double y, double z) {
        ImmutableVector extents = getHalfExtents();
        ImmutableVector newExtents = new ImmutableVector(extents.getX() * x, extents.getY() * y, extents.getZ() * z);
        ImmutableVector diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public AxisAlignedBoundingBoxCollider scale(double amount) {
        ImmutableVector extents = getHalfExtents();
        ImmutableVector newExtents = extents.multiply(amount);
        ImmutableVector diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public AxisAlignedBoundingBoxCollider grow(Vector diff) {
        return new AxisAlignedBoundingBoxCollider(world, min.subtract(diff), max.add(diff), position);
    }
}
