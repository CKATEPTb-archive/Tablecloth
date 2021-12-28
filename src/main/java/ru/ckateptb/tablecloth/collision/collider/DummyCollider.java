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

import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.Vector3d;

/**
 * Dummy {@link AABB} collider for passable blocks
 */
public final class DummyCollider extends AABB {
    public static final AABB INSTANCE = new DummyCollider();

    private DummyCollider() {
        super(Vector3d.ZERO, Vector3d.ZERO);
    }

    @Override
    public AABB grow(Vector3d diff) {
        return this;
    }

    @Override
    public boolean intersects(Collider collider) {
        return false;
    }

    @Override
    public boolean intersects(Ray ray) {
        return false;
    }

    @Override
    public Vector3d getPosition() {
        return Vector3d.ZERO;
    }

    @Override
    public AABB at(Vector3d pos) {
        return this;
    }

    @Override
    public Vector3d getHalfExtents() {
        return Vector3d.ZERO;
    }

    @Override
    public boolean contains(Vector3d point) {
        return false;
    }
}
