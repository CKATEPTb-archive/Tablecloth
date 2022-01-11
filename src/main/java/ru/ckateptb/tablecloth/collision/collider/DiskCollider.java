package ru.ckateptb.tablecloth.collision.collider;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.ImmutableVector;

@Getter
public class DiskCollider extends CompositeCollider {
    private final OrientedBoundingBoxCollider orientedBoundingBoxCollider;
    private final SphereCollider sphereCollider;

    public DiskCollider(World world, Vector position, OrientedBoundingBoxCollider orientedBoundingBoxCollider, SphereCollider sphereCollider) {
        super(world, position, orientedBoundingBoxCollider, sphereCollider);
        this.orientedBoundingBoxCollider = orientedBoundingBoxCollider;
        this.sphereCollider = sphereCollider;
    }

    @Override
    public boolean intersects(Collider collider) {
        return super.allIntersects(collider);
    }

    @Override
    public DiskCollider at(Vector point) {
        return new DiskCollider(world, point, orientedBoundingBoxCollider.at(point), sphereCollider.at(point));
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return orientedBoundingBoxCollider.getHalfExtents();
    }

    @Override
    public boolean contains(ImmutableVector point) {
        return allContains(point);
    }
}
