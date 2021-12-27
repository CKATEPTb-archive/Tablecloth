package ru.ckateptb.tablecloth.util;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class VectorUtils {
    /**
     * Null vector (coordinates: 0, 0, 0).
     */
    public static final Vector ZERO = new Vector(0, 0, 0);

    /**
     * First canonical vector (coordinates: 1, 0, 0).
     */
    public static final Vector PLUS_I = new Vector(1, 0, 0);

    /**
     * Opposite of the first canonical vector (coordinates: -1, 0, 0).
     */
    public static final Vector MINUS_I = new Vector(-1, 0, 0);

    /**
     * Second canonical vector (coordinates: 0, 1, 0).
     */
    public static final Vector PLUS_J = new Vector(0, 1, 0);

    /**
     * Opposite of the second canonical vector (coordinates: 0, -1, 0).
     */
    public static final Vector MINUS_J = new Vector(0, -1, 0);

    /**
     * Third canonical vector (coordinates: 0, 0, 1).
     */
    public static final Vector PLUS_K = new Vector(0, 0, 1);

    /**
     * Opposite of the third canonical vector (coordinates: 0, 0, -1).
     */
    public static final Vector MINUS_K = new Vector(0, 0, -1);

    // CHECKSTYLE: stop ConstantName
    /**
     * A vector with all coordinates set to NaN.
     */
    public static final Vector NaN = new Vector(Double.NaN, Double.NaN, Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /**
     * A vector with all coordinates set to positive infinity.
     */
    public static final Vector POSITIVE_INFINITY = new Vector(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /**
     * A vector with all coordinates set to negative infinity.
     */
    public static final Vector NEGATIVE_INFINITY = new Vector(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    public static Vector normalizeOrElse(Vector vector, Vector otherwise) {
        if (getNormSq(vector) == 0) {
            return otherwise;
        }
        return vector.normalize();
    }

    public static double getNormSq(Vector vector) {
        double[] ar = toArray(vector);
        // there are no cancellation problems here, so we use the straightforward formula
        return ar[0] * ar[0] + ar[1] * ar[1] + ar[2] * ar[2];
    }

    public static Vector hadamard(Vector a, Vector b) {
        return new Vector(a.getX() * b.getX(), a.getY() * b.getY(), a.getZ() * b.getZ());
    }

    public static double[] toArray(Vector vector) {
        return new double[]{vector.getX(), vector.getY(), vector.getZ()};
    }

    public static Vector fromArray(double[] vector) {
        return new Vector(vector[0], vector[1], vector[2]);
    }

    public static double component(Vector vec, int axis) {
        return toArray(vec)[axis];
    }

    public static Vector setComponent(Vector vec, int axis, double value) {
        double[] values = toArray(vec);
        values[axis] = value;
        return new Vector(values[0], values[1], values[2]);
    }

    public static Vector setX(Vector vec, double x) {
        return new Vector(x, vec.getY(), vec.getZ());
    }

    public static Vector setY(Vector vec, double y) {
        return new Vector(vec.getX(), y, vec.getZ());
    }

    public static Vector setZ(Vector vec, double z) {
        return new Vector(vec.getX(), vec.getY(), z);
    }

    public static Vector clearAxis(Vector vec, int axis) {
        if (axis == 0) {
            return new Vector(0, vec.getY(), vec.getZ());
        } else if (axis == 1) {
            return new Vector(vec.getX(), 0, vec.getZ());
        } else {
            return new Vector(vec.getX(), vec.getY(), 0);
        }
    }

    public static Vector applyRotation(Rotation rotation, Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        double q0 = rotation.getQ0();
        double q1 = rotation.getQ1();
        double q2 = rotation.getQ2();
        double q3 = rotation.getQ3();

        double s = q1 * x + q2 * y + q3 * z;

        return new Vector(2 * (q0 * (x * q0 - (q2 * z - q3 * y)) + s * q1) - x,
                2 * (q0 * (y * q0 - (q3 * x - q1 * z)) + s * q2) - y,
                2 * (q0 * (z * q0 - (q1 * y - q2 * x)) + s * q3) - z);
    }

    public static Vector getAxisVector(Vector vec, int axis) {
        if (axis == 0) {
            return new Vector(vec.getX(), 0, 0);
        } else if (axis == 1) {
            return new Vector(0, vec.getY(), 0);
        } else {
            return new Vector(0, 0, vec.getZ());
        }
    }

    public static Vector getBlockVector(Vector vector) {
        return new Vector(Math.floor(vector.getX()), Math.floor(vector.getY()), Math.floor(vector.getZ()));
    }

    public static double getMinComponent(Vector vector) {
        return Math.min(vector.getX(), Math.min(vector.getY(), vector.getZ()));
    }

    public static double getMaxComponent(Vector vector) {
        return Math.max(vector.getX(), Math.max(vector.getY(), vector.getZ()));
    }

    public static Vector rotateYaw(Vector vector, double rads) {
        return rotate(vector, PLUS_J, rads);
    }

    public static Vector rotatePitch(Vector vector, double rads) {
        Vector axis = VectorUtils.normalizeOrElse(vector.crossProduct(PLUS_J), PLUS_I);
        return rotate(vector, axis, rads);
    }

    public static Vector rotate(Vector vector, Vector axis, double rads) {
        Vector a = vector.multiply(Math.cos(rads));
        Vector b = axis.crossProduct(vector).multiply(Math.sin(rads));
        Vector c = axis.multiply(axis.dot(vector)).multiply(1 - Math.cos(rads));

        return a.add(b).add(c);
    }

    public static Vector fromYaw(double yaw) {
        double x = -Math.sin(Math.toRadians(yaw));
        double z = Math.cos(Math.toRadians(yaw));

        return new Vector(x, 0, z);
    }

    public static Vector fromTo(Location from, Location to) {
        return to.clone().subtract(from).toVector().normalize();
    }
}
