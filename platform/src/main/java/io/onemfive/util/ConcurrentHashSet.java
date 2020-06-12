package io.onemfive.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Implement on top of a ConcurrentHashMap with a dummy value.
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {
    private static final Object DUMMY = new Object();
    private final Map<E, Object> map;

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<E, Object>();
    }
    public ConcurrentHashSet(int capacity) {
        map = new ConcurrentHashMap<E, Object>(capacity);
    }

    @Override
    public boolean add(E o) {
        return map.put(o, DUMMY) == null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    public int size() {
        return map.size();
    }

    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean rv = false;
        for (E e : c)
            rv |= map.put(e, DUMMY) == null;
        return rv;
    }
}
