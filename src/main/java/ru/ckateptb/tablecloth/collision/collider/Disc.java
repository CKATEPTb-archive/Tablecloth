/*
 * Copyright 2020-2021 Moros
 *
 * This file is part of Bending.
 *
 * Bending is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bending is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bending. If not, see <https://www.gnu.org/licenses/>.
 */

package ru.ckateptb.tablecloth.collision.collider;

import org.bukkit.Location;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.Vector3d;

/**
 * Combination of {@link OBB} and {@link Sphere} to simulate a disk collider
 */
public class Disc implements Collider {
    private final Sphere sphere;
    private final OBB obb;

    public Disc(OBB obb, Sphere sphere) {
        this.obb = obb;
        this.sphere = sphere;
    }

    public Disc addPosition(Vector3d position) {
        return new Disc(this.obb.addPosition(position), this.sphere.at(position));
    }

    public Disc addPosition(Location position) {
        return addPosition(new Vector3d(position.toVector()));
    }

    @Override
    public boolean intersects(Collider collider) {
        return sphere.intersects(collider) && obb.intersects(collider);
    }

    @Override
    public Vector3d getPosition() {
        return sphere.center;
    }

    @Override
    public Disc at(Vector3d position) {
        return new Disc(obb.at(position), sphere.at(position));
    }

    @Override
    public Vector3d getHalfExtents() {
        return obb.getHalfExtents();
    }

    @Override
    public boolean contains(Vector3d point) {
        return sphere.contains(point) && obb.contains(point);
    }
}
