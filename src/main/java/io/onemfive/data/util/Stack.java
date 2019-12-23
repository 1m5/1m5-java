package io.onemfive.data.util;

import java.io.Serializable;
import java.util.Iterator;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface Stack<T> extends Serializable {
    void push(T object);
    T pop();
    T peek();
    Integer numberRemainingRoutes();
    Iterator<T> getIterator();
}
