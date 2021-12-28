package ru.ckateptb.tablecloth.math;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Immutable 3D Vector implementation with integer coordinates
 */
public class Vector3i {
    public static final Vector3i ZERO = new Vector3i(0, 0, 0);

    private final int x;
    private final int y;
    private final int z;

    /**
     * Build a vector from its coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3i(Block b) {
        this(b.getX(), b.getY(), b.getZ());
    }

    /**
     * Build a vector from its coordinates.
     *
     * @param v coordinates array
     * @throws IllegalArgumentException if array does not have 3 elements
     * @see #toArray()
     */
    public Vector3i(int[] v) throws IllegalArgumentException {
        if (v.length != 3) {
            throw new IllegalArgumentException();
        }
        this.x = v[0];
        this.y = v[1];
        this.z = v[2];
    }

    /**
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return a new vector copy with the given x coordinate
     */
    public Vector3i setX(int value) {
        return new Vector3i(value, y, z);
    }

    /**
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * @return a new vector copy with the given y coordinate
     */
    public Vector3i setY(int value) {
        return new Vector3i(x, value, z);
    }

    /**
     * @return the z coordinate
     */
    public int getZ() {
        return z;
    }

    /**
     * @return a new vector copy with the given z coordinate
     */
    public Vector3i setZ(int value) {
        return new Vector3i(x, y, value);
    }

    /**
     * Get the vector coordinates as a dimension 3 array.
     *
     * @return vector coordinates
     * @see #Vector3i(int[])
     */
    public int[] toArray() {
        return new int[]{x, y, z};
    }

    /**
     * @return Euclidean norm for the vector
     */
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * @return square of the Euclidean norm for the vector
     */
    public int lengthSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Add a vector to the instance.
     *
     * @param v vector to add
     * @return a new vector
     */
    public Vector3i add(Vector3i v) {
        return new Vector3i(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Subtract a vector from the instance.
     *
     * @param v vector to subtract
     * @return a new vector
     */
    public Vector3i subtract(Vector3i v) {
        return new Vector3i(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Get the opposite of the instance.
     *
     * @return a new vector which is opposite to the instance
     */
    public Vector3i negate() {
        return new Vector3i(-x, -y, -z);
    }

    /**
     * Multiply the instance by a scalar.
     *
     * @param a scalar
     * @return a new vector
     */
    public Vector3i multiply(int a) {
        return new Vector3i(a * x, a * y, a * z);
    }

    public Vector3i multiply(Vector3i v) {
        return new Vector3i(x * v.x, y * v.y, z * v.z);
    }

    public int dot(Vector3i v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector3i cross(Vector3i v) {
        int newX = y * v.z - v.y * z;
        int newY = z * v.x - v.z * x;
        int newZ = x * v.y - v.x * y;
        return new Vector3i(newX, newY, newZ);
    }

    public double distance(Vector3i v) {
        int dx = v.x - x;
        int dy = v.y - y;
        int dz = v.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public int distanceSq(Vector3i v) {
        int dx = v.x - x;
        int dy = v.y - y;
        int dz = v.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Vector3i v) {
            return (x == v.x) && (y == v.y) && (z == v.z);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }

    public Block toBlock(World world) {
        return world.getBlockAt(x, y, z);
    }
}
