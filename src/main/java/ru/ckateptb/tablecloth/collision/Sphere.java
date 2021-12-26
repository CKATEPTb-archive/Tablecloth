package ru.ckateptb.tablecloth.collision;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.util.WorldUtils;

import java.util.Collection;

public class Sphere implements Collider {
    public Vector center;
    public double radius;
    public World world;

    public Sphere(Vector center, double radius) {
        this(center, radius, null);
    }

    public Sphere(Location center, double radius) {
        this(center.toVector(), radius, center.getWorld());
    }

    public Sphere(Vector center, double radius, World world) {
        this.center = center;
        this.radius = radius;
        this.world = world;
    }

    public Sphere at(Vector newCenter) {
        return new Sphere(newCenter, radius, world);
    }

    public Sphere at(Location newCenter) {
        return new Sphere(newCenter, radius);
    }

    public boolean intersects(AABB aabb) {
        if (this.world != null && aabb.getWorld() != null && !aabb.getWorld().equals(this.world)) {
            return false;
        }

        Vector min = aabb.min();
        Vector max = aabb.max();

        if (min == null || max == null) return false;

        // Get the point closest to sphere center on the aabb.
        double x = Math.max(min.getX(), Math.min(center.getX(), max.getX()));
        double y = Math.max(min.getY(), Math.min(center.getY(), max.getY()));
        double z = Math.max(min.getZ(), Math.min(center.getZ(), max.getZ()));

        // Check if that point is inside of the sphere.
        return contains(new Vector(x, y, z));
    }

    public boolean intersects(OBB obb) {
        if (this.world != null && obb.getWorld() != null && !obb.getWorld().equals(this.world)) {
            return false;
        }

        Vector closest = obb.getClosestPosition(center);
        Vector v = center.subtract(closest);

        return v.dot(v) <= radius * radius;
    }

    public boolean intersects(Sphere other) {
        if (this.world != null && other.getWorld() != null && !other.getWorld().equals(this.world)) {
            return false;
        }

        double distSq = other.center.distanceSquared(center);
        double rsum = radius + other.radius;

        // Spheres will be colliding if their distance apart is less than the sum of the radii.
        return distSq <= rsum * rsum;
    }

    public boolean intersects(Ray ray) {
        Vector m = ray.origin.subtract(center);
        double b = m.dot(ray.direction);

        // Use quadratic equation to solve ray-sphere intersection.
        double discriminant = b * b - (m.dot(m) - radius * radius);

        return discriminant >= 0;
    }

    @Override
    public boolean intersects(Collider collider) {
        if (this.world != null && collider.getWorld() != null && !collider.getWorld().equals(this.world)) {
            return false;
        }

        if (collider instanceof Sphere) {
            return intersects((Sphere) collider);
        } else if (collider instanceof AABB) {
            return intersects((AABB) collider);
        } else if (collider instanceof OBB) {
            return intersects((OBB) collider);
        } else if (collider instanceof Disc) {
            return collider.intersects(this);
        }

        return false;
    }

    @Override
    public Vector getPosition() {
        return center;
    }

    @Override
    public Vector getHalfExtents() {
        return new Vector(radius, radius, radius);
    }

    @Override
    public World getWorld() {
        return world;
    }

    public boolean contains(Vector point) {
        double distSq = center.distanceSquared(point);
        return distSq <= radius * radius;
    }

    public Collection<Block> handleBlockCollisions(Material... ignored) {
        return WorldUtils.getNearbyBlocks(center.toLocation(world), radius, ignored);
    }
}
