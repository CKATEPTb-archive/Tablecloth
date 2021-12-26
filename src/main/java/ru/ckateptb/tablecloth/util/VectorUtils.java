package ru.ckateptb.tablecloth.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class VectorUtils {
    private VectorUtils() {

    }

    public static Vector normalizeOrElse(Vector vector, Vector otherwise) {
        if (AdaptUtils.adapt(vector).getNormSq() == 0) {
            return otherwise;
        }
        return vector.normalize();
    }

    public static Vector hadamard(Vector a, Vector b) {
        return new Vector(a.getX() * b.getX(), a.getY() * b.getY(), a.getZ() * b.getZ());
    }

    public static double component(Vector vec, int axis) {
        return AdaptUtils.adapt(vec).toArray()[axis];
    }

    public static double component(Vector3D vec, int axis) {
        return vec.toArray()[axis];
    }

    public static Vector setComponent(Vector vec, int axis, double value) {
        double[] values = AdaptUtils.adapt(vec).toArray();
        values[axis] = value;
        return AdaptUtils.adapt(new Vector3D(values));
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
        return rotate(vector, AdaptUtils.adapt(Vector3D.PLUS_J), rads);
    }

    public static Vector rotatePitch(Vector vector, double rads) {
        Vector axis = VectorUtils.normalizeOrElse(vector.crossProduct(AdaptUtils.adapt(Vector3D.PLUS_J))
                , AdaptUtils.adapt(Vector3D.PLUS_I));
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
