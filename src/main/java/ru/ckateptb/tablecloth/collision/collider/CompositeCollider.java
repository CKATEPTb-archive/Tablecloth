package ru.ckateptb.tablecloth.collision.collider;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.AbstractCollider;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.ImmutableVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CompositeCollider extends AbstractCollider {
    private final List<Collider> colliders = new ArrayList<>();
    private final ImmutableVector position;

    public CompositeCollider(World world, Vector position, Collider... colliders) {
        super(world);
        this.colliders.addAll(Arrays.asList(colliders));
        this.position = new ImmutableVector(position);
    }

    public boolean allIntersects(Collider other) {
        return colliders.stream().allMatch(collider -> collider.intersects(other));
    }

    public boolean anyIntersects(Collider other) {
        return colliders.stream().anyMatch(collider -> collider.intersects(other));
    }

    @Override
    public boolean intersects(Collider collider) {
        return anyIntersects(collider);
    }

    @Override
    public ImmutableVector getPosition() {
        return this.position;
    }

    @Override
    public CompositeCollider at(Vector point) {
        CompositeCollider compositeCollider = new CompositeCollider(world, new ImmutableVector(point));
        for (Collider collider : colliders) {
            compositeCollider.colliders.add(collider.at(point));
        }
        return compositeCollider;
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return ImmutableVector.ONE;
    }

    public boolean allContains(ImmutableVector point) {
        return colliders.stream().allMatch(collider -> collider.contains(point));
    }

    public boolean anyContains(ImmutableVector point) {
        return colliders.stream().anyMatch(collider -> collider.contains(point));
    }

    @Override
    public boolean contains(ImmutableVector point) {
        return anyContains(point);
    }
}
