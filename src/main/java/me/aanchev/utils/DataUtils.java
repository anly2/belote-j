package me.aanchev.utils;

import java.util.AbstractMap;
import java.util.Map;

public class DataUtils {
    private DataUtils() {}


    public static <K, V> Map.Entry<K, V> pair(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

}
