package ru.ckateptb.tablecloth.math;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FastMath {
    public static int floor(double num) {
        int y = (int) num;
        if (num < y) {
            return y - 1;
        }
        return y;
    }

    public static int ceil(double num) {
        int y = (int) num;
        if (num > y) {
            return y + 1;
        }
        return y;
    }

    public static int round(double num) {
        return floor(num + 0.5);
    }
}
