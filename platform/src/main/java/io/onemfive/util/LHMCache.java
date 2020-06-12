package io.onemfive.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  A LinkedHashMap with a maximum size, for use as an LRU cache.
 *  Unsynchronized.
 */
public class LHMCache<K, V> extends LinkedHashMap<K, V> {

    private final int max;

    public LHMCache(int max) {
        super(max, 0.75f, true);
        this.max = max;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > max;
    }
}
