package ru.ckateptb.tablecloth.collision;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.util.VectorUtils;


// Oriented bounding box
public class OBB implements Collider {
    private final Vector center;
    private final RealMatrix basis;
    // Half extents in local space.
    private final Vector e;
    private final World world;

    public OBB(Vector center, Vector halfExtents, Vector axis0, Vector axis1, Vector axis2) {
        this(center, halfExtents, axis0, axis1, axis2, null);
    }

    public OBB(Vector center, Vector halfExtents, Vector axis0, Vector axis1, Vector axis2, World world) {
        this.center = center;
        this.e = halfExtents;
        this.basis = MatrixUtils.createRealMatrix(3, 3);
        this.basis.setRow(0, VectorUtils.toArray(axis0));
        this.basis.setRow(1, VectorUtils.toArray(axis1));
        this.basis.setRow(2, VectorUtils.toArray(axis2));
        this.world = world;
    }

    public OBB(Vector center, RealMatrix basis, Vector halfExtents) {
        this(center, basis, halfExtents, null);
    }

    public OBB(Vector center, RealMatrix basis, Vector halfExtents, World world) {
        this.center = center;
        this.basis = basis;
        this.e = halfExtents;
        this.world = world;
    }

    public OBB(AABB aabb) {
        this(aabb, (World) null);
    }

    public OBB(AABB aabb, World world) {
        this.center = aabb.getPosition();
        this.basis = MatrixUtils.createRealIdentityMatrix(3);
        this.e = aabb.getHalfExtents();
        this.world = world;
    }

    public OBB(AABB aabb, Rotation rotation) {
        this(aabb, rotation, null);
    }

    public OBB(AABB aabb, Rotation rotation, World world) {
        this.center = VectorUtils.applyRotation(rotation, aabb.getPosition());
        this.basis = MatrixUtils.createRealMatrix(rotation.getMatrix());
        this.e = aabb.getHalfExtents();
        this.world = world;
    }

    public OBB addPosition(Vector position) {
        return new OBB(center.add(position), basis, e, world);
    }

    public OBB addPosition(Location location) {
        return new OBB(center.add(location.toVector()), basis, e, location.getWorld());
    }

    public OBB at(Vector position) {
        return new OBB(position, basis, e, world);
    }

    public OBB at(Location location) {
        return new OBB(location.toVector(), basis, e, location.getWorld());
    }

    @Override
    public boolean intersects(Collider collider) {
        if (this.world != null && collider.getWorld() != null && !collider.getWorld().equals(this.world)) {
            return false;
        }

        if (collider instanceof Sphere) {
            return ((Sphere) collider).intersects(this);
        } else if (collider instanceof AABB) {
            return intersects(new OBB((AABB) collider, collider.getWorld()));
        } else if (collider instanceof OBB) {
            return intersects((OBB) collider);
        } else if (collider instanceof Disc) {
            return collider.intersects(this);
        }

        return false;
    }

    public boolean intersects(OBB other) {
        if (this.world != null && other.getWorld() != null && !other.getWorld().equals(this.world)) {
            return false;
        }

        final double epsilon = 0.000001;
        double ra, rb;

        RealMatrix R = getRotationMatrix(other);
        // translation
        Vector t = other.center.subtract(center);
        // Bring into coordinate frame
        t = VectorUtils.fromArray(basis.operate(VectorUtils.toArray(t)));
        RealMatrix absR = MatrixUtils.createRealMatrix(3, 3);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                absR.setEntry(i, j, Math.abs(R.getEntry(i, j)) + epsilon);
            }
        }

        // test this box's axes
        for (int i = 0; i < 3; ++i) {
            Vector row = VectorUtils.fromArray(absR.getRow(i));

            ra = VectorUtils.component(e, i);
            rb = other.e.dot(row);

            if (Math.abs(VectorUtils.component(t, i)) > ra + rb) {
                return false;
            }
        }

        // test other box's axes
        for (int i = 0; i < 3; ++i) {
            Vector col = VectorUtils.fromArray(absR.getColumn(i));

            ra = e.dot(col);
            rb = VectorUtils.component(other.e, i);

            Vector rotCol = VectorUtils.fromArray(R.getColumn(i));
            if (Math.abs(t.dot(rotCol)) > ra + rb) {
                return false;
            }
        }

        // A0 x B0
        ra = VectorUtils.component(e, 1) * absR.getEntry(2, 0) + VectorUtils.component(e, 2) * absR.getEntry(1, 0);
        rb = VectorUtils.component(other.e, 1) * absR.getEntry(0, 2) + VectorUtils.component(other.e, 2) * absR.getEntry(0, 1);
        if (Math.abs(VectorUtils.component(t, 2) * R.getEntry(1, 0) - VectorUtils.component(t, 1) * R.getEntry(2, 0)) > ra + rb) {
            return false;
        }

        // A0 x B1
        ra = VectorUtils.component(e, 1) * absR.getEntry(2, 1) + VectorUtils.component(e, 2) * absR.getEntry(1, 1);
        rb = VectorUtils.component(other.e, 0) * absR.getEntry(0, 2) + VectorUtils.component(other.e, 2) * absR.getEntry(0, 0);
        if (Math.abs(VectorUtils.component(t, 2) * R.getEntry(1, 1) - VectorUtils.component(t, 1) * R.getEntry(2, 1)) > ra + rb) {
            return false;
        }

        // A0 x B2
        ra = VectorUtils.component(e, 1) * absR.getEntry(2, 2) + VectorUtils.component(e, 2) * absR.getEntry(1, 2);
        rb = VectorUtils.component(other.e, 0) * absR.getEntry(0, 1) + VectorUtils.component(other.e, 1) * absR.getEntry(0, 0);
        if (Math.abs(VectorUtils.component(t, 2) * R.getEntry(1, 2) - VectorUtils.component(t, 1) * R.getEntry(2, 2)) > ra + rb) {
            return false;
        }

        // A1 x B0
        ra = VectorUtils.component(e, 0) * absR.getEntry(2, 0) + VectorUtils.component(e, 2) * absR.getEntry(0, 0);
        rb = VectorUtils.component(other.e, 1) * absR.getEntry(1, 2) + VectorUtils.component(other.e, 2) * absR.getEntry(1, 1);
        if (Math.abs(VectorUtils.component(t, 0) * R.getEntry(2, 0) - VectorUtils.component(t, 2) * R.getEntry(0, 0)) > ra + rb) {
            return false;
        }

        // A1 x B1
        ra = VectorUtils.component(e, 0) * absR.getEntry(2, 1) + VectorUtils.component(e, 2) * absR.getEntry(0, 1);
        rb = VectorUtils.component(other.e, 0) * absR.getEntry(1, 2) + VectorUtils.component(other.e, 2) * absR.getEntry(1, 0);
        if (Math.abs(VectorUtils.component(t, 0) * R.getEntry(2, 1) - VectorUtils.component(t, 2) * R.getEntry(0, 1)) > ra + rb) {
            return false;
        }

        // A1 x B2
        ra = VectorUtils.component(e, 0) * absR.getEntry(2, 2) + VectorUtils.component(e, 2) * absR.getEntry(0, 2);
        rb = VectorUtils.component(other.e, 0) * absR.getEntry(1, 1) + VectorUtils.component(other.e, 1) * absR.getEntry(1, 0);
        if (Math.abs(VectorUtils.component(t, 0) * R.getEntry(2, 2) - VectorUtils.component(t, 2) * R.getEntry(0, 2)) > ra + rb) {
            return false;
        }

        // A2 x B0
        ra = VectorUtils.component(e, 0) * absR.getEntry(1, 0) + VectorUtils.component(e, 1) * absR.getEntry(0, 0);
        rb = VectorUtils.component(other.e, 1) * absR.getEntry(2, 2) + VectorUtils.component(other.e, 2) * absR.getEntry(2, 1);
        if (Math.abs(VectorUtils.component(t, 1) * R.getEntry(0, 0) - VectorUtils.component(t, 0) * R.getEntry(1, 0)) > ra + rb) {
            return false;
        }

        // A2 x B1
        ra = VectorUtils.component(e, 0) * absR.getEntry(1, 1) + VectorUtils.component(e, 1) * absR.getEntry(0, 1);
        rb = VectorUtils.component(other.e, 0) * absR.getEntry(2, 2) + VectorUtils.component(other.e, 2) * absR.getEntry(2, 0);
        if (Math.abs(VectorUtils.component(t, 1) * R.getEntry(0, 1) - VectorUtils.component(t, 0) * R.getEntry(1, 1)) > ra + rb) {
            return false;
        }

        // A2 x B2
        ra = VectorUtils.component(e, 0) * absR.getEntry(1, 2) + VectorUtils.component(e, 1) * absR.getEntry(0, 2);
        rb = VectorUtils.component(other.e, 0) * absR.getEntry(2, 1) + VectorUtils.component(other.e, 1) * absR.getEntry(2, 0);
        return !(Math.abs(VectorUtils.component(t, 1) * R.getEntry(0, 2) - VectorUtils.component(t, 0) * R.getEntry(1, 2)) > ra + rb);
    }

    // Express the other box's basis in this box's coordinate frame.
    private RealMatrix getRotationMatrix(OBB other) {
        RealMatrix r = MatrixUtils.createRealMatrix(3, 3);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Vector a = VectorUtils.fromArray(basis.getRow(i));
                Vector b = VectorUtils.fromArray(other.basis.getRow(j));

                r.setEntry(i, j, a.dot(b));
            }
        }

        return r;
    }

    // Returns the position closest to the target that lies on/in the OBB.
    public Vector getClosestPosition(Vector target) {
        Vector t = target.subtract(center);
        Vector closest = center;

        // Project target onto basis axes and move toward it.
        for (int i = 0; i < 3; ++i) {
            Vector axis = VectorUtils.fromArray(basis.getRow(i));
            double r = VectorUtils.component(e, i);
            double dist = Math.max(-r, Math.min(t.dot(axis), r));

            closest = closest.add(axis.multiply(dist));
        }

        return closest;
    }

    @Override
    public Vector getPosition() {
        return center;
    }

    @Override
    public Vector getHalfExtents() {
        double x = e.dot(VectorUtils.PLUS_I);
        double y = e.dot(VectorUtils.PLUS_J);
        double z = e.dot(VectorUtils.PLUS_K);

        return new Vector(x, y, z);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public boolean contains(Vector point) {
        double epsilon = 0.001;
        return getClosestPosition(point).distanceSquared(point) <= epsilon;
    }

    public Vector getHalfDiagonal() {
        Vector result = VectorUtils.ZERO.clone();

        for (int i = 0; i < 3; ++i) {
            result = result.add(VectorUtils.fromArray(basis.getRow(i)).multiply(VectorUtils.component(e, i)));
        }

        return result;
    }
}

