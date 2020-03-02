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
package io.onemfive.util;

import java.io.Serializable;
import java.util.*;

/**
 * Properties map that has its keySet ordered consistently (via the key's lexicographical ordering).
 * This is useful in environments where maps must stay the same order (e.g. for signature verification)
 * This does NOT support remove against the iterators / etc.
 *
 * Now unsorted until the keyset or entryset is requested.
 * The class is unsynchronized.
 * The keySet() and entrySet() methods return ordered sets.
 * Others - such as the enumerations values(), keys(), propertyNames() - do not.
 */
public class OrderedProperties extends Properties {

    public OrderedProperties() {
        super();
    }

    @Override
    public Set<Object> keySet() {
        if (size() <= 1)
            return super.keySet();
        return Collections.unmodifiableSortedSet(new TreeSet<Object>(super.keySet()));
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        if (size() <= 1)
            return super.entrySet();
        TreeSet<Map.Entry<Object, Object>> rv = new TreeSet<Map.Entry<Object, Object>>(new EntryComparator());
        rv.addAll(super.entrySet());
        return Collections.unmodifiableSortedSet(rv);
    }

    private static class EntryComparator implements Comparator<Map.Entry<Object, Object>>, Serializable {
        public int compare(Map.Entry<Object, Object> l, Map.Entry<Object, Object> r) {
            return ((String)l.getKey()).compareTo(((String)r.getKey()));
        }
    }
}
