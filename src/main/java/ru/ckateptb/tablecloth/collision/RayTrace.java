package ru.ckateptb.tablecloth.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import ru.ckateptb.tablecloth.math.Vector3d;
import ru.ckateptb.tablecloth.math.Vector3i;
import ru.ckateptb.tablecloth.util.WorldUtils;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class RayTrace {
    private static final double MAX_RANGE = 100;
    private Vector3d origin;
    private Vector3d direction;
    private double range;
    private double raySize = 0;
    private boolean ignoreLiquids = true;
    private boolean ignorePassable = true;
    private Type type = Type.BLOCK;
    private Set<BlockPos> ignoreBlocks = Set.of();
    private Predicate<Entity> entityPredicate = x -> true;
    private RayTrace(Vector3d origin, Vector3d direction) {
        this.origin = origin;
        this.direction = direction.normalize();
        range(direction.length());
    }

    public static RayTrace of(LivingEntity user) {
        Location eyeLocation = user.getEyeLocation();
        return new RayTrace(new Vector3d(eyeLocation), new Vector3d(eyeLocation.getDirection()));
    }

    public static RayTrace of(Vector3d origin, Vector3d direction) {
        Objects.requireNonNull(origin);
        Objects.requireNonNull(direction);
        return new RayTrace(origin, direction);
    }

    public RayTrace origin(Vector3d origin) {
        this.origin = Objects.requireNonNull(origin);
        return this;
    }

    public RayTrace direction(Vector3d direction) {
        this.direction = direction.normalize();
        return this;
    }

    public RayTrace range(double range) {
        this.range = Math.min(MAX_RANGE, Math.max(0, range));
        return this;
    }

    public RayTrace ignoreLiquids(boolean ignoreLiquids) {
        this.ignoreLiquids = ignoreLiquids;
        return this;
    }

    public RayTrace ignorePassable(boolean ignorePassable) {
        this.ignorePassable = ignorePassable;
        return this;
    }

    public RayTrace type(Type type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }

    public RayTrace raySize(double raySize) {
        this.raySize = Math.max(0, raySize);
        return this;
    }

    public RayTrace ignore(Set<Block> ignoreBlocks) {
        this.ignoreBlocks = Objects.requireNonNull(ignoreBlocks).stream()
                .map(b -> new BlockPos(b.getX(), b.getY(), b.getZ())).collect(Collectors.toSet());
        return this;
    }

    public RayTrace filter(Predicate<Entity> entityPredicate) {
        this.entityPredicate = Objects.requireNonNull(entityPredicate);
        return this;
    }

    private BlockHit checkBlockCollision(Level world, ClipContext context, BlockPos pos, BlockHit miss) {
        if (ignoreBlocks.contains(pos)) {
            return null;
        }
        BlockState iblockdata = world.getBlockState(pos);
        if (iblockdata.isAir()) return null;
        FluidState fluid = iblockdata.getFluidState();
        Vec3 vec3d = context.getFrom();
        Vec3 vec3d1 = context.getTo();
        VoxelShape voxelshape = context.getBlockShape(iblockdata, world, pos);
        BlockHitResult res0 = world.clipWithInteractionOverride(vec3d, vec3d1, pos, voxelshape, iblockdata);
        VoxelShape voxelshape1 = context.getFluidShape(fluid, world, pos);
        BlockHitResult res1 = voxelshape1.clip(vec3d, vec3d1, pos);
        double d0 = res0 == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(res0.getLocation());
        double d1 = res1 == null ? Double.MAX_VALUE : context.getFrom().distanceToSqr(res1.getLocation());
        BlockHit b0 = res0 == null ? null : new BlockHit(res0);
        BlockHit b1 = res1 == null ? null : new BlockHit(res1);
        return d0 <= d1 ? b0 : b1;
    }

    private CompositeResult rayTraceBlocks(World world, ClipContext clipContext) {
        Level nmsWorld = ((CraftWorld) world).getHandle();
        BlockHit miss = new BlockHit(clipContext.getTo(), new BlockPos(clipContext.getTo()));
        BlockHit result = traverseBlocks(nmsWorld, clipContext, miss);
        if (result != null && !miss.equals(result)) {
            Vector3i bp = result.blockPosition();
            Block block = world.getBlockAt(bp.getX(), bp.getY(), bp.getZ());
            return new CompositeResult(result.position(), block, null);
        }
        return new CompositeResult(miss.position(), null, null);
    }

    private BlockHit traverseBlocks(Level level, ClipContext context, BlockHit miss) {
        Vec3 start = context.getFrom();
        Vec3 end = context.getTo();
        if (start.equals(end)) {
            return miss;
        } else {
            double d0 = Mth.lerp(-1.0E-7D, end.x, start.x);
            double d1 = Mth.lerp(-1.0E-7D, end.y, start.y);
            double d2 = Mth.lerp(-1.0E-7D, end.z, start.z);
            double d3 = Mth.lerp(-1.0E-7D, start.x, end.x);
            double d4 = Mth.lerp(-1.0E-7D, start.y, end.y);
            double d5 = Mth.lerp(-1.0E-7D, start.z, end.z);
            int i = Mth.floor(d3);
            int j = Mth.floor(d4);
            int k = Mth.floor(d5);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, j, k);
            BlockHit t0 = checkBlockCollision(level, context, mutableBlockPos, miss);
            if (t0 != null) {
                return t0;
            } else {
                double d6 = d0 - d3;
                double d7 = d1 - d4;
                double d8 = d2 - d5;
                int l = Mth.sign(d6);
                int i1 = Mth.sign(d7);
                int j1 = Mth.sign(d8);
                double d9 = l == 0 ? Double.MAX_VALUE : (double) l / d6;
                double d10 = i1 == 0 ? Double.MAX_VALUE : (double) i1 / d7;
                double d11 = j1 == 0 ? Double.MAX_VALUE : (double) j1 / d8;
                double d12 = d9 * (l > 0 ? 1.0D - Mth.frac(d3) : Mth.frac(d3));
                double d13 = d10 * (i1 > 0 ? 1.0D - Mth.frac(d4) : Mth.frac(d4));
                double d14 = d11 * (j1 > 0 ? 1.0D - Mth.frac(d5) : Mth.frac(d5));
                BlockHit result;
                do {
                    if (d12 > 1.0D && d13 > 1.0D && d14 > 1.0D) {
                        return miss;
                    }
                    if (d12 < d13) {
                        if (d12 < d14) {
                            i += l;
                            d12 += d9;
                        } else {
                            k += j1;
                            d14 += d11;
                        }
                    } else if (d13 < d14) {
                        j += i1;
                        d13 += d10;
                    } else {
                        k += j1;
                        d14 += d11;
                    }
                    result = checkBlockCollision(level, context, mutableBlockPos.set(i, j, k), miss);
                } while (result == null);
                return result;
            }
        }
    }

    public CompositeResult result(World world) {
        return result(world, type);
    }

    public CompositeResult result(World world, Type type) {
        boolean checkEntities = type != Type.BLOCK;

        Vec3 startPos = new Vec3(origin.getX(), origin.getY(), origin.getZ());
        Vector3d endPoint = origin.add(direction.multiply(range));
        Vec3 endPos = new Vec3(endPoint.getX(), endPoint.getY(), endPoint.getZ());

        ClipContext.Block ccb = ignorePassable ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;
        ClipContext.Fluid ccf = ignoreLiquids ? Fluid.NONE : Fluid.ANY;
        ClipContext clipContext = new ClipContext(startPos, endPos, ccb, ccf, null);

        CompositeResult blockResult = rayTraceBlocks(world, clipContext);
        double blockHitDistance = blockResult.hit ? origin.distance(blockResult.position) : range;

        CompositeResult entityResult = new CompositeResult(endPoint, null, null);
        if (checkEntities) {
            Location start = origin.toLocation(world);
            RayTraceResult eResult = world.rayTraceEntities(start, direction.toBukkitVector(), blockHitDistance, raySize, entityPredicate);
            Vector3d pos = eResult == null ? endPoint : new Vector3d(eResult.getHitPosition());
            Entity entity = eResult == null ? null : eResult.getHitEntity();
            entityResult = new CompositeResult(pos, null, entity);
        }
        if (!blockResult.hit) {
            return entityResult;
        }
        if (!entityResult.hit) {
            return blockResult;
        }
        double entityHitDistanceSquared = origin.distanceSq(entityResult.position);
        if (entityHitDistanceSquared < (blockHitDistance * blockHitDistance)) {
            return entityResult;
        }
        return blockResult;
    }

    public enum Type {COMPOSITE, ENTITY, BLOCK}

    private static final record BlockHit(Vector3d position, Vector3i blockPosition) {
        private BlockHit(BlockHitResult hitResult) {
            this(hitResult.getLocation(), hitResult.getBlockPos());
        }

        private BlockHit(Vec3 pos, BlockPos bp) {
            this(new Vector3d(pos.x, pos.y, pos.z), new Vector3i(bp.getX(), bp.getY(), bp.getZ()));
        }
    }

    public static final class CompositeResult {
        private final Vector3d position;
        private final Block block;
        private final Entity entity;

        private final boolean hit;

        private CompositeResult(Vector3d position, Block block, Entity entity) {
            this.position = position;
            this.block = block;
            this.entity = entity;
            hit = block != null || entity != null;
        }

        public Vector3d position() {
            return position;
        }

        public Block block() {
            return block;
        }

        public Entity entity() {
            return entity;
        }

        public Vector3d entityCenterOrPosition() {
            return entity == null ? position : WorldUtils.getEntityCenter(entity);
        }

        public Vector3d entityEyeLevelOrPosition() {
            if (entity instanceof LivingEntity livingEntity) {
                return new Vector3d(livingEntity.getEyeLocation());
            }
            return position;
        }

        public boolean hit() {
            return hit;
        }
    }
}
