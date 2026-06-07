package me.aanchev.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LatchUtils {
    public static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.HOURS)) {
                throw new IllegalStateException("Timed out");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
