package com.dreamer.userservice.utils;

public class LevelUtil {

    private static final int K = 100;

    public static int calculateLevel(int exp) {
        if (exp <= 0) {
            return 1;
        }
        return (int) Math.sqrt(exp / (double) K) + 1;
    }
}