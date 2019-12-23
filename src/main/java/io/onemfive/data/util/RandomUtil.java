package io.onemfive.data.util;

import java.util.Random;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class RandomUtil {
    public static long nextRandomLong() {
        return new Random(System.currentTimeMillis()).nextLong();
    }
    public static int nextRandomInteger() { return new Random(System.currentTimeMillis()).nextInt(); }
    public static int nextRandomInteger(int upperBound) { return new Random(System.currentTimeMillis()).nextInt(upperBound); }
}
