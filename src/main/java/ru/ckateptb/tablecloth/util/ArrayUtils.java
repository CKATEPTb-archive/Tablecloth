package ru.ckateptb.tablecloth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArrayUtils {
    /**
     * Соединить два массива в один
     *
     * @param A первый массив
     * @param B второй массив
     * @return результат из двух массивов
     */
    public static Object[] concatenate(Object[] A, Object[] B) {
        int aLen = A.length, bLen = B.length;
        Object[] C = (Object[]) Array.newInstance(A.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }
}
