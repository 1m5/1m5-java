package io.onemfive.util;

import java.util.Random;

/**
 * TODO: Add Description
 *
 */
public final class Numbers {

    public static int randomNumber(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

}
