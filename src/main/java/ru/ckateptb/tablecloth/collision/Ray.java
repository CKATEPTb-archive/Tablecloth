package ru.ckateptb.tablecloth.collision;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Ray {
    public Vector origin;
    public Vector direction;
    public Vector directionReciprocal;

    public Ray(Vector origin, Vector direction) {
        this.direction = direction;
        this.origin = origin;
        this.directionReciprocal = new Vector(0, 0, 0);

        if (direction.getX() == 0.0) {
            directionReciprocal = new Vector(Double.MAX_VALUE, directionReciprocal.getY(), directionReciprocal.getZ());
        } else {
            directionReciprocal = new Vector(1.0 / direction.getX(), directionReciprocal.getY(), directionReciprocal.getZ());
        }

        if (direction.getY() == 0.0) {
            directionReciprocal = new Vector(directionReciprocal.getX(), Double.MAX_VALUE, directionReciprocal.getZ());
        } else {
            directionReciprocal = new Vector(directionReciprocal.getX(), 1.0 / direction.getY(), directionReciprocal.getZ());
        }

        if (direction.getZ() == 0.0) {
            directionReciprocal = new Vector(directionReciprocal.getX(), directionReciprocal.getY(), Double.MAX_VALUE);
        } else {
            directionReciprocal = new Vector(directionReciprocal.getX(), directionReciprocal.getY(), 1.0 / direction.getZ());
        }
    }

    public Ray(Location origin, Vector direction) {
        this(origin.toVector(), direction);
    }
}

