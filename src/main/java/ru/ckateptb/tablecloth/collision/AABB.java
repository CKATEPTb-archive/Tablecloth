package ru.ckateptb.tablecloth.collision;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.util.VectorUtils;

import java.util.Optional;

public class AABB implements Collider {
    public static final AABB PLAYER_BOUNDS = new AABB(new Vector(-0.3, 0.0, -0.3), new Vector(0.3, 1.8, 0.3), null);
    public static final AABB BLOCK_BOUNDS = new AABB(new Vector(0.0, 0.0, 0.0), new Vector(1.0, 1.0, 1.0), null);

    private final Vector min;
    private final Vector max;
    private final World world;

    public AABB(Vector min, Vector max) {
        this(min, max, null);
    }

    public AABB(Vector min, Vector max, World world) {
        this.min = min;
        this.max = max;
        this.world = world;
    }

    public AABB at(Vector pos) {
        if (min == null || max == null) return new AABB(null, null, world);

        return new AABB(min.add(pos), max.add(pos), world);
    }

    public AABB at(Location location) {
        if (min == null || max == null) return new AABB(null, null, location.getWorld());

        return at(location.toVector());
    }

    public AABB grow(double x, double y, double z) {
        Vector change = new Vector(x, y, z);

        return new AABB(min.subtract(change), max.add(change), this.world);
    }

    public AABB scale(double x, double y, double z) {
        Vector extents = getHalfExtents();
        Vector newExtents = VectorUtils.hadamard(extents, new Vector(x, y, z));
        Vector diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public AABB scale(double amount) {
        Vector extents = getHalfExtents();
        Vector newExtents = extents.multiply(amount);
        Vector diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public Vector min() {
        return this.min;
    }

    public Vector max() {
        return this.max;
    }

    public Vector mid() {
        return this.min.add(this.max().subtract(this.min()).multiply(0.5));
    }

    public boolean contains(Vector test) {
        if (min == null || max == null) return false;

        return (test.getX() >= min.getX() && test.getX() <= max.getX()) &&
                (test.getY() >= min.getY() && test.getY() <= max.getY()) &&
                (test.getZ() >= min.getZ() && test.getZ() <= max.getZ());
    }

    public Optional<Double> intersects(Ray ray) {
        if (min == null || max == null) return Optional.empty();

        double t1 = (min.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();
        double t2 = (max.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();

        double t3 = (min.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();
        double t4 = (max.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();

        double t5 = (min.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();
        double t6 = (max.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax) {
            return Optional.empty();
        }

        return Optional.of(tmin);
    }

    public boolean intersects(AABB other) {
        if (this.world != null && other.getWorld() != null && !other.getWorld().equals(this.world)) {
            return false;
        }

        if (min == null || max == null || other.min == null || other.max == null) {
            return false;
        }

        return (max.getX() > other.min.getX() &&
                min.getX() < other.max.getX() &&
                max.getY() > other.min.getY() &&
                min.getY() < other.max.getY() &&
                max.getZ() > other.min.getZ() &&
                min.getZ() < other.max.getZ());
    }

    public boolean intersects(Sphere sphere) {
        if (this.world != null && sphere.getWorld() != null && !sphere.getWorld().equals(this.world)) {
            return false;
        }

        return sphere.intersects(this);
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
            return collider.intersects(this);
        } else if (collider instanceof Disc) {
            return collider.intersects(this);
        }

        return false;
    }

    @Override
    public Vector getPosition() {
        return mid();
    }

    @Override
    public Vector getHalfExtents() {
        if (max == null || min == null) return new Vector(0, 0, 0);

        Vector half = max.subtract(min).multiply(0.5);
        return new Vector(Math.abs(half.getX()), Math.abs(half.getY()), Math.abs(half.getZ()));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "[AABB min: " + min + ", max: " + max + "]";
    }
}

