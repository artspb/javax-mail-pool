package me.artspb.javax.mail.util;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Artem Khvastunov
 */
public class MapUtils {

    private MapUtils() {
    }

    public static <K, V> V putOrGet(ConcurrentMap<K, V> map, K key, V candidate) {
        V value;
        if ((value = map.putIfAbsent(key, candidate)) == null) {
            value = candidate;
        }
        return value;
    }
}
