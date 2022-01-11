package ru.ckateptb.tablecloth.collision;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.math.ImmutableVector;

@Getter
@Setter
public abstract class AbstractCollider implements Collider {
    protected World world;

    public AbstractCollider(World world) {
        this.world = world;
    }

    @Override
    public boolean contains(Vector point) {
        return contains(new ImmutableVector(point));
    }

    @Override
    public Location getLocation() {
        return getPosition().toLocation(world);
    }

    @Override
    public boolean contains(Location location) {
        return world.equals(location.getWorld()) && contains(location.toVector());
    }
}
