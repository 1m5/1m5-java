/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.core.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Implement on top of a ConcurrentHashMap with a dummy value.
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {
    private static final Object DUMMY = new Object();
    private final Map<E, Object> _map;

    public ConcurrentHashSet() {
        _map = new ConcurrentHashMap<E, Object>();
    }
    public ConcurrentHashSet(int capacity) {
        _map = new ConcurrentHashMap<E, Object>(capacity);
    }

    @Override
    public boolean add(E o) {
        return _map.put(o, DUMMY) == null;
    }

    @Override
    public void clear() {
        _map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return _map.containsKey(o);
    }

    @Override
    public boolean isEmpty() {
        return _map.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        return _map.remove(o) != null;
    }

    public int size() {
        return _map.size();
    }

    public Iterator<E> iterator() {
        return _map.keySet().iterator();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean rv = false;
        for (E e : c)
            rv |= _map.put(e, DUMMY) == null;
        return rv;
    }
}
