package ru.ckateptb.tablecloth.collision;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

// Combines an OBB and Sphere to create a disc-like collider.
public class Disc implements Collider {
    private final Sphere sphere;
    private final OBB obb;

    public Disc(OBB obb, Sphere sphere) {
        this.obb = obb;
        this.sphere = sphere;
    }

    public Disc addPosition(Vector position) {
        return new Disc(this.obb.addPosition(position), this.sphere.at(position));
    }

    public Disc addPosition(Location position) {
        return new Disc(this.obb.addPosition(position), this.sphere.at(position));
    }

    public Disc at(Vector position) {
        return new Disc(this.obb.at(position), this.sphere.at(position));
    }

    public Disc at(Location position) {
        return new Disc(this.obb.at(position), this.sphere.at(position));
    }

    @Override
    public boolean intersects(Collider collider) {
        return sphere.intersects(collider) && obb.intersects(collider);
    }

    @Override
    public Vector getPosition() {
        return sphere.center;
    }

    @Override
    public Vector getHalfExtents() {
        return obb.getHalfExtents();
    }

    @Override
    public World getWorld() {
        return obb.getWorld();
    }

    @Override
    public boolean contains(Vector point) {
        return sphere.contains(point) && obb.contains(point);
    }

    public OBB getOBB() {
        return obb;
    }

    public Sphere getSphere() {
        return sphere;
    }
}

