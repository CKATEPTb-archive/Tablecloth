package ru.ckateptb.tablecloth.util;

import com.comphenix.protocol.utility.StreamSerializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdaptUtils {
    private static final char[] VALUES = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static Location toLocation(String string) {
        if (string == null) return null;
        String[] args = string.split(";");
        if (args.length != 4) return null;
        World world = Bukkit.getWorld(args[0]);
        if (world == null) return null;
        double x = Double.parseDouble(args[1]);
        double y = Double.parseDouble(args[2]);
        double z = Double.parseDouble(args[3]);
        return new Location(world, x, y, z);
    }

    public static String toString(Location location) {
        if (location == null) return null;
        String world = location.getWorld().getName();
        String x = String.valueOf(location.getX());
        String y = String.valueOf(location.getY());
        String z = String.valueOf(location.getZ());
        return String.join(";", world, x, y, z);
    }

    public static String toString(List<ItemStack> itemStacks) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null) continue;
            try {
                stringBuilder.append(StreamSerializer.getDefault().serializeItemStack(itemStack));
                stringBuilder.append(";");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    public static List<ItemStack> toListOfItemStack(String string) {
        List<ItemStack> itemStacks = new ArrayList<>();
        if (string != null) {
            String[] strings = string.split(";");
            for (String itemString : strings) {
                if (itemString.isEmpty()) continue;
                try {
                    itemStacks.add(StreamSerializer.getDefault().deserializeItemStack(itemString));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return itemStacks;
    }

    public static ItemStack toItemStack(String string) {
        if (!string.isEmpty()) {
            try {
                return StreamSerializer.getDefault().deserializeItemStack(string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String adapt(ItemStack itemStack) {
        try {
            return StreamSerializer.getDefault().serializeItemStack(itemStack);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String toHex(int r, int g, int b) {
        if (r < 0) {
            r = 0;
        } else if (r > 255) {
            r = 255;
        }

        if (g < 0) {
            g = 0;
        } else if (g > 255) {
            g = 255;
        }

        if (b < 0) {
            b = 0;
        } else if (b > 255) {
            b = 255;
        }

        int[] rgb = {r, g, b};
        StringBuilder hex = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            int first = (int) Math.floor(rgb[i] / 16F);
            int second = rgb[i] % 16;

            hex.append(VALUES[first]);
            hex.append(VALUES[second]);
        }

        return hex.toString();
    }

    public static int[] toRGB(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        String red = hex.substring(0, 2);
        String green = hex.substring(2, 4);
        String blue = hex.substring(4, 6);

        char[] reds = red.toCharArray();
        char[] greens = green.toCharArray();
        char[] blues = blue.toCharArray();

        int redVal = Arrays.binarySearch(VALUES, reds[0]) * 16 + Arrays.binarySearch(VALUES, reds[1]);
        int greenVal = Arrays.binarySearch(VALUES, greens[0]) * 16 + Arrays.binarySearch(VALUES, greens[1]);
        int blueVal = Arrays.binarySearch(VALUES, blues[0]) * 16 + Arrays.binarySearch(VALUES, blues[1]);

        return new int[]{redVal, greenVal, blueVal};
    }
}
