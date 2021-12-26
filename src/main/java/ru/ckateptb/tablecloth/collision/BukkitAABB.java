package ru.ckateptb.tablecloth.collision;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BukkitAABB {
    private static Class<?> CraftWorld, CraftEntity, World, AxisAlignedBB, Block, BlockPosition, IBlockData, Entity, VoxelShape;
    private static Method getHandle, getEntityHandle, getType, getBlock, getBlockBoundingBox, getBoundingBox, getVoxelShape, isEmptyBounds;
    private static Field minXField, minYField, minZField, maxXField, maxYField, maxZField;
    private static Constructor<?> bpConstructor;
    private static int serverVersion;

    static {
        serverVersion = 9;

        try {
            serverVersion = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().substring(23).split("_")[1]);
        } catch (Exception e) {

        }

        if (!setupReflection()) {
            Bukkit.getLogger().warning("ERROR: Failed to setup BukkitAABB reflection.");
        }
    }

    private final Vector min = null;
    private final Vector max = null;

    private BukkitAABB() {

    }

    public static AABB getBlockBounds(Block block) {
        Vector min = getBlockMin(block);
        Vector max = getBlockMax(block);

        return new AABB(min, max, block.getWorld());
    }

    public static AABB getEntityBounds(Entity entity) {
        Vector min = getEntityMin(entity);
        Vector max = getEntityMax(entity);

        if (min != null) {
            min = min.subtract(entity.getLocation().toVector());
        }

        if (max != null) {
            max = max.subtract(entity.getLocation().toVector());
        }

        return new AABB(min, max, entity.getLocation().getWorld());
    }

    private static Vector getEntityMin(Entity entity) {
        try {
            Object handle = getEntityHandle.invoke(CraftEntity.cast(entity));
            Object aabb = getBoundingBox.invoke(handle);

            if (aabb == null) return null;

            double a = (double) minXField.get(aabb);
            double b = (double) minYField.get(aabb);
            double c = (double) minZField.get(aabb);

            return new Vector(a, b, c);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector getEntityMax(Entity entity) {
        try {
            Object handle = getEntityHandle.invoke(CraftEntity.cast(entity));
            Object aabb = getBoundingBox.invoke(handle);

            if (aabb == null) return null;

            double d = (double) maxXField.get(aabb);
            double e = (double) maxYField.get(aabb);
            double f = (double) maxZField.get(aabb);

            return new Vector(d, e, f);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector getBlockMin(Block block) {
        Object aabb = getAABB(block);
        if (aabb == null) return null;

        try {
            double a = (double) minXField.get(aabb);
            double b = (double) minYField.get(aabb);
            double c = (double) minZField.get(aabb);

            return new Vector(a, b, c);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Vector getBlockMax(Block block) {
        Object aabb = getAABB(block);
        if (aabb == null) return null;

        try {
            double d = (double) maxXField.get(aabb);
            double e = (double) maxYField.get(aabb);
            double f = (double) maxZField.get(aabb);

            return new Vector(d, e, f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Object getAABB(Block block) {
        if (serverVersion < 13) {
            try {
                Object bp = bpConstructor.newInstance(block.getX(), block.getY(), block.getZ());
                Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
                Object blockData = getType.invoke(world, BlockPosition.cast(bp));
                Object blockNative = getBlock.invoke(blockData);

                return getBlockBoundingBox.invoke(blockNative, IBlockData.cast(blockData), World.cast(world), BlockPosition.cast(bp));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Object bp = bpConstructor.newInstance(block.getX(), block.getY(), block.getZ());
                Object world = getHandle.invoke(CraftWorld.cast(block.getWorld()));
                Object blockData = getType.invoke(world, BlockPosition.cast(bp));
                Object blockNative = getBlock.invoke(blockData);
                Object voxelShape = getVoxelShape.invoke(blockNative, IBlockData.cast(blockData), World.cast(world), BlockPosition.cast(bp));
                Object emptyBounds = isEmptyBounds.invoke(voxelShape);

                if (emptyBounds != null && !(Boolean) emptyBounds) {
                    return getBlockBoundingBox.invoke(voxelShape);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static boolean setupReflection() {
        CraftWorld = getNMSClass("org.bukkit.craftbukkit.%s.CraftWorld");
        CraftEntity = getNMSClass("org.bukkit.craftbukkit.%s.entity.CraftEntity");
        World = getNMSClass("net.minecraft.server.%s.World");
        AxisAlignedBB = getNMSClass("net.minecraft.server.%s.AxisAlignedBB");
        Block = getNMSClass("net.minecraft.server.%s.Block");
        BlockPosition = getNMSClass("net.minecraft.server.%s.BlockPosition");
        IBlockData = getNMSClass("net.minecraft.server.%s.IBlockData");
        Entity = getNMSClass("net.minecraft.server.%s.Entity");
        VoxelShape = getNMSClass("net.minecraft.server.%s.VoxelShape");
        Class<?> IBlockAccess = getNMSClass("net.minecraft.server.%s.IBlockAccess");

        try {
            getHandle = CraftWorld.getDeclaredMethod("getHandle");
            getEntityHandle = CraftEntity.getDeclaredMethod("getHandle");
            getType = World.getDeclaredMethod("getType", BlockPosition);
            getBlock = IBlockData.getDeclaredMethod("getBlock");
            bpConstructor = BlockPosition.getConstructor(int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }

        try {
            if (serverVersion < 13) {
                getBlockBoundingBox = Block.getDeclaredMethod("a", IBlockData, serverVersion >= 11 ? IBlockAccess : World, BlockPosition);
                getBoundingBox = Entity.getDeclaredMethod("getBoundingBox");
            } else {
                getVoxelShape = Block.getDeclaredMethod("f", IBlockData, IBlockAccess, BlockPosition);

                try {
                    getBlockBoundingBox = VoxelShape.getDeclaredMethod("getBoundingBox");
                } catch (NoSuchMethodException e) {
                    getBlockBoundingBox = VoxelShape.getDeclaredMethod("a");
                }

                getBoundingBox = Entity.getDeclaredMethod("getBoundingBox");

                try {
                    isEmptyBounds = VoxelShape.getDeclaredMethod("b");
                } catch (NoSuchMethodException e) {
                    isEmptyBounds = VoxelShape.getDeclaredMethod("isEmpty");
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }

        try {
            minXField = AxisAlignedBB.getField("a");
            minYField = AxisAlignedBB.getField("b");
            minZField = AxisAlignedBB.getField("c");
            maxXField = AxisAlignedBB.getField("d");
            maxYField = AxisAlignedBB.getField("e");
            maxZField = AxisAlignedBB.getField("f");
        } catch (NoSuchFieldException e) {
            try {
                minXField = AxisAlignedBB.getField("minX");
                minYField = AxisAlignedBB.getField("minY");
                minZField = AxisAlignedBB.getField("minZ");
                maxXField = AxisAlignedBB.getField("maxX");
                maxYField = AxisAlignedBB.getField("maxY");
                maxZField = AxisAlignedBB.getField("maxZ");
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private static Class<?> getNMSClass(String nmsClass) {
        String version = null;

        Pattern pattern = Pattern.compile("net\\.minecraft\\.(?:server)?\\.(v(?:\\d+_)+R\\d)");
        for (Package p : Package.getPackages()) {
            String name = p.getName();
            Matcher m = pattern.matcher(name);
            if (m.matches()) {
                version = m.group(1);
            }
        }

        if (version == null) return null;

        try {
            return Class.forName(String.format(nmsClass, version));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
