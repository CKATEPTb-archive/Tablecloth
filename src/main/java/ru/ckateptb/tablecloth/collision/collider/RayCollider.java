package ru.ckateptb.tablecloth.collision.collider;

import lombok.Getter;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import ru.ckateptb.tablecloth.collision.AbstractCollider;
import ru.ckateptb.tablecloth.collision.Collider;
import ru.ckateptb.tablecloth.math.ImmutableVector;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public class RayCollider extends AbstractCollider {
    private double maxDistance;
    private final double raySize;
    private final ImmutableVector original;
    private final ImmutableVector direction;

    public RayCollider(LivingEntity livingEntity, double maxDistance) {
        this(livingEntity, maxDistance, 0);
    }

    public RayCollider(LivingEntity livingEntity, double maxDistance, double raySize) {
        super(livingEntity.getWorld());
        Location eyeLocation = livingEntity.getEyeLocation();
        this.original = new ImmutableVector(eyeLocation);
        this.direction = new ImmutableVector(eyeLocation.getDirection());
        this.maxDistance = maxDistance;
        this.raySize = raySize;
    }

    public RayCollider(World world, Vector original, Vector direction, double maxDistance, double raySize) {
        super(world);
        this.original = new ImmutableVector(original);
        this.direction = new ImmutableVector(direction);
        this.maxDistance = maxDistance;
        this.raySize = raySize;
    }

    @Override
    public boolean intersects(Collider collider) {
        return toBoundingBoxCollider().intersects(collider);
    }

    @Override
    public ImmutableVector getPosition() {
        return this.original;
    }

    @Override
    public RayCollider at(Vector point) {
        return new RayCollider(world, point, direction, maxDistance, raySize);
    }

    public AxisAlignedBoundingBoxCollider toBoundingBoxCollider() {
        BoundingBox boundingBox = BoundingBox.of(original, original).expandDirectional(direction.normalize().multiply(maxDistance)).expand(raySize);
        return new AxisAlignedBoundingBoxCollider(world, boundingBox.getMin(), boundingBox.getMax());
    }

    @Override
    public ImmutableVector getHalfExtents() {
        return toBoundingBoxCollider().getHalfExtents();
    }

    @Override
    public boolean contains(ImmutableVector point) {
        return toBoundingBoxCollider().contains(point);
    }

    public Optional<Map.Entry<Block, BlockFace>> getFirstBlock(boolean ignoreLiquids, boolean ignorePassable) {
        RayTraceResult traceResult = world.rayTraceBlocks(original.toLocation(world), direction, maxDistance, ignoreLiquids ? FluidCollisionMode.NEVER : FluidCollisionMode.ALWAYS, ignorePassable);
        if (traceResult == null) return Optional.empty();
        Block block = traceResult.getHitBlock();
        BlockFace blockFace = traceResult.getHitBlockFace();
        return block == null || blockFace == null ? Optional.empty() : Optional.of(Map.entry(block, blockFace));
    }

    public Optional<Block> getBlock(boolean ignoreLiquids, boolean ignorePassable, Predicate<Block> filter) {
        return this.getBlock(ignoreLiquids, ignorePassable, true, filter);
    }

    public Optional<Block> getBlock(boolean ignoreLiquids, boolean ignorePassable, boolean ignoreBlockBounds, Predicate<Block> filter) {
        BlockIterator it = new BlockIterator(world, original, direction, raySize, Math.min(100, (int) Math.ceil(maxDistance)));
        while (it.hasNext()) {
            Block block = it.next();
            if (ignoreLiquids && block.isLiquid()) {
                continue;
            }
            if (ignorePassable && block.isPassable()) {
                continue;
            }
            if (filter.test(block)) {
                return Optional.of(block);
            }
            if (!ignoreBlockBounds && block.isPassable()) {
                break;
            }
        }
        return Optional.empty();
    }

    public Optional<Entity> getEntity(Predicate<Entity> filter) {
        RayTraceResult traceResult = world.rayTraceEntities(original.toLocation(world), direction, maxDistance, raySize, filter);
        if (traceResult == null) return Optional.empty();
        return Optional.ofNullable(traceResult.getHitEntity());
    }

    public Optional<Vector> getPosition(boolean ignoreEntity, boolean ignoreBlock, boolean ignoreLiquid, boolean ignorePassable, Predicate<Entity> entityFilter, Predicate<Block> blockFilter) {
        final double maxDistance = this.maxDistance;

        Vector blockPosition = null;
        Vector entityPosition = null;
        Vector position = original.add(direction.normalize().multiply(maxDistance));

        if (!ignoreBlock) {
            Optional<Block> optional = getBlock(ignoreLiquid, ignorePassable, true, blockFilter);
            if (optional.isPresent()) {
                Block block = optional.get();
                ImmutableVector immutableVector = new ImmutableVector(block.getLocation());
                blockPosition = original.subtract(immutableVector).normalize();
                this.maxDistance = original.distance(blockPosition);
            }
        }

        if (!ignoreEntity) {
            Optional<Entity> optional = getEntity(entityFilter);
            if(optional.isPresent()) {
                Entity entity = optional.get();
                entityPosition = new ImmutableVector(entity.getLocation()).add(new ImmutableVector(0, entity.getHeight() / 2, 0));
            }
        }
        this.maxDistance = maxDistance;
        return Optional.of(entityPosition == null ? blockPosition == null ? position : blockPosition : entityPosition);
    }
}
