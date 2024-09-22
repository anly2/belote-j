package me.aanchev.utils;

import java.util.concurrent.CountDownLatch;

public class LatchUtils {
    public static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, java.util.concurrent.TimeUnit.MINUTES)) {
                throw new IllegalStateException("Timed out");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
