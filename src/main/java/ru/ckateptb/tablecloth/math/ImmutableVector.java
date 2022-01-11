package ru.ckateptb.tablecloth.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.ckateptb.tablecloth.collision.collider.AxisAlignedBoundingBoxCollider;
import ru.ckateptb.tablecloth.collision.collider.RayCollider;
import ru.ckateptb.tablecloth.collision.collider.SphereCollider;

import java.util.Map;

public class ImmutableVector extends Vector {
    public static final ImmutableVector ZERO = new ImmutableVector(0, 0, 0);
    public static final ImmutableVector ONE = new ImmutableVector(1, 1, 1);
    public static final ImmutableVector PLUS_I = new ImmutableVector(1, 0, 0);
    public static final ImmutableVector MINUS_I = new ImmutableVector(-1, 0, 0);
    public static final ImmutableVector PLUS_J = new ImmutableVector(0, 1, 0);
    public static final ImmutableVector MINUS_J = new ImmutableVector(0, -1, 0);
    public static final ImmutableVector PLUS_K = new ImmutableVector(0, 0, 1);
    public static final ImmutableVector MINUS_K = new ImmutableVector(0, 0, -1);
    public static final ImmutableVector MIN_VELOCITY = new ImmutableVector(-4, -4, -4);
    public static final ImmutableVector MAX_VELOCITY = new ImmutableVector(4, 4, 4);

    public ImmutableVector() {
        super();
    }

    public ImmutableVector(Vector vector) {
        super(vector.getX(), vector.getY(), vector.getZ());
    }

    public ImmutableVector(Location location) {
        this(location.getX(), location.getY(), location.getZ());
    }

    public ImmutableVector(Block block) {
        this(block.getX(), block.getY(), block.getZ());
    }

    public ImmutableVector(Entity entity) {
        this(entity.getLocation());
    }

    public ImmutableVector(int x, int y, int z) {
        super(x, y, z);
    }

    public ImmutableVector(double x, double y, double z) {
        super(x, y, z);
    }

    public ImmutableVector(float x, float y, float z) {
        super(x, y, z);
    }

    public ImmutableVector(double[] coords) {
        super(coords[0], coords[1], coords[2]);
    }

    @Override
    public @NotNull ImmutableVector add(@NotNull Vector vector) {
        return new ImmutableVector(x + vector.getX(), y + vector.getY(), z + vector.getZ());
    }

    @Override
    public @NotNull ImmutableVector subtract(@NotNull Vector vector) {
        return new ImmutableVector(x - vector.getX(), y - vector.getY(), z - vector.getZ());
    }

    public ImmutableVector normalize() {
        return normalizeOrElse(ImmutableVector.PLUS_I);
    }

    public ImmutableVector normalizeOrElse(ImmutableVector def) {
        double s = length();
        if (s == 0) {
            return def;
        }
        return multiply(1 / s);
    }

    @Override
    public @NotNull ImmutableVector multiply(int m) {
        return new ImmutableVector(m * x, m * y, m * z);
    }

    public ImmutableVector multiply(double m) {
        return new ImmutableVector(m * x, m * y, m * z);
    }

    @Override
    public @NotNull ImmutableVector multiply(float m) {
        return new ImmutableVector(m * x, m * y, m * z);
    }

    @Override
    public @NotNull ImmutableVector multiply(@NotNull Vector vector) {
        return new ImmutableVector(x * vector.getX(), y * vector.getY(), z * vector.getZ());
    }

    @Override
    public @NotNull ImmutableVector divide(@NotNull Vector vector) {
        return new ImmutableVector(x / vector.getX(), y / vector.getY(), z / vector.getZ());
    }

    @Override
    public @NotNull ImmutableVector copy(@NotNull Vector vector) {
        return new ImmutableVector(vector);
    }

    @Override
    public @NotNull ImmutableVector midpoint(@NotNull Vector other) {
        return new ImmutableVector((x + other.getX()) / 2, (y + other.getY()) / 2, (z + other.getZ()) / 2);
    }

    @Override
    public @NotNull ImmutableVector getMidpoint(@NotNull Vector other) {
        return midpoint(other);
    }

    @Override
    public @NotNull ImmutableVector crossProduct(@NotNull Vector vector) {
        double newX = y * vector.getZ() - vector.getY() * z;
        double newY = z * vector.getX() - vector.getZ() * x;
        double newZ = x * vector.getY() - vector.getX() * y;
        return new ImmutableVector(newX, newY, newZ);
    }

    @Override
    public @NotNull ImmutableVector getCrossProduct(@NotNull Vector vector) {
        return crossProduct(vector);
    }

    @Override
    public @NotNull ImmutableVector zero() {
        return ImmutableVector.ZERO;
    }

    public boolean isInAABB(@NotNull AxisAlignedBoundingBoxCollider collider) {
        return isInAABB(collider.getMin(), collider.getMax());
    }

    public boolean isInSphere(@NotNull SphereCollider collider) {
        return isInSphere(collider.getCenter(), collider.getRadius());
    }

    @Override
    public @NotNull ImmutableVector rotateAroundX(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double y = angleCos * getY() - angleSin * getZ();
        double z = angleSin * getY() + angleCos * getZ();
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector rotateAroundY(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * getX() + angleSin * getZ();
        double z = -angleSin * getX() + angleCos * getZ();
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector rotateAroundZ(double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);

        double x = angleCos * getX() - angleSin * getY();
        double y = angleSin * getX() + angleCos * getY();
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector rotateAroundAxis(@NotNull Vector axis, double angle) throws IllegalArgumentException {
        return rotateAroundNonUnitAxis(axis.isNormalized() ? axis : axis.clone().normalize(), angle);
    }

    @Override
    public @NotNull ImmutableVector rotateAroundNonUnitAxis(@NotNull Vector axis, double angle) throws IllegalArgumentException {
        double x = getX(), y = getY(), z = getZ();
        double x2 = axis.getX(), y2 = axis.getY(), z2 = axis.getZ();

        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);
        double dotProduct = this.dot(axis);

        double xPrime = x2 * dotProduct * (1d - cosTheta)
                + x * cosTheta
                + (-z2 * y + y2 * z) * sinTheta;
        double yPrime = y2 * dotProduct * (1d - cosTheta)
                + y * cosTheta
                + (z2 * x - x2 * z) * sinTheta;
        double zPrime = z2 * dotProduct * (1d - cosTheta)
                + z * cosTheta
                + (-y2 * x + x2 * y) * sinTheta;

        return new ImmutableVector(xPrime, yPrime, zPrime);
    }

    @Override
    public @NotNull ImmutableVector setX(int x) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setX(double x) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setX(float x) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setY(int y) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setY(double y) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setY(float y) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setZ(int z) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setZ(double z) {
        return new ImmutableVector(x, y, z);
    }

    @Override
    public @NotNull ImmutableVector setZ(float z) {
        return new ImmutableVector(x, y, z);
    }

    public ImmutableVector min(Vector vector) {
        return new ImmutableVector(Math.min(x, vector.getX()), Math.min(y, vector.getY()), Math.min(z, vector.getZ()));
    }

    public ImmutableVector max(Vector vector) {
        return new ImmutableVector(Math.max(x, vector.getX()), Math.max(y, vector.getY()), Math.max(z, vector.getZ()));
    }

    public ImmutableVector abs() {
        return new ImmutableVector(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public ImmutableVector floor() {
        return new ImmutableVector(Math.floor(x), Math.floor(y), Math.floor(z));
    }


    public ImmutableVector rotate(ImmutableVector axis, double rads) {
        ImmutableVector a = this.multiply(Math.cos(rads));
        ImmutableVector b = axis.crossProduct(this).multiply(Math.sin(rads));
        ImmutableVector c = axis.multiply(axis.dot(this)).multiply(1 - Math.cos(rads));
        return a.add(b).add(c);
    }

    public ImmutableVector snapToBlockCenter() {
        double newX = Math.floor(x) + 0.5;
        double newY = Math.floor(y) + 0.5;
        double newZ = Math.floor(z) + 0.5;
        return new ImmutableVector(newX, newY, newZ);
    }

    public ImmutableVector clampVelocity() {
        return min(MAX_VELOCITY).max(MIN_VELOCITY);
    }

    public ImmutableVector negate() {
        return multiply(-1);
    }

    public double[] toArray() {
        return new double[]{x, y, z};
    }

    public double minComponent() {
        return Math.min(x, Math.min(y, z));
    }

    public double maxComponent() {
        return Math.max(x, Math.max(y, z));
    }

    public double getDistanceAboveGround(World world) {
        return getDistanceAboveGround(world, true);
    }

    public double getDistanceAboveGround(World world, boolean ignoreLiquids) {
        return y - new RayCollider(world, this, MINUS_J, Math.min(world.getMaxHeight(), y), 0)
                .getFirstBlock(ignoreLiquids, true)
                .map(Map.Entry::getKey)
                .map(block -> new AxisAlignedBoundingBoxCollider(block).at(new ImmutableVector(block)).getMax().getY())
                .orElse(0d);
    }
}
