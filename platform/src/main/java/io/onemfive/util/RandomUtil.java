package io.onemfive.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    public static long nextRandomLong() {
        return ThreadLocalRandom.current().nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }
    public static long nextRandomLong(long upperBound) {
        return ThreadLocalRandom.current().nextLong(Long.MIN_VALUE, upperBound);
    }
    public static long nextRandomLong(long lowerBound, long upperbound) {
        return ThreadLocalRandom.current().nextLong(lowerBound, upperbound);
    }
    public static int nextRandomInteger() {
        return ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    public static int nextRandomInteger(int upperBound) {
        return ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, upperBound);
    }

    public static int nextRandomInteger(int lowerBound, int upperBound) {
        return ThreadLocalRandom.current().nextInt(lowerBound, upperBound);
    }
}
