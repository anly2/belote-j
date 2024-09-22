package me.aanchev.utils;

public class Bytes {
    private Bytes() {}

    public static int indexOf(byte[] array, byte element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == element) return i;
        }
        return -1;
    }
}
