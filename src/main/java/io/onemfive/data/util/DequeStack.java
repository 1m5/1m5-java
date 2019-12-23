package io.onemfive.data.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Stack implementation using a Deque.
 *
 * @author objectorange
 */
public class DequeStack<T> implements Stack<T> {

    private final Deque<T> deque = new ArrayDeque<>();

    @Override
    public void push(T object) {
        deque.addFirst(object);
    }

    @Override
    public T pop() {
        try {
            return deque.removeFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public T peek() {
        return deque.peekFirst();
    }

    @Override
    public Integer numberRemainingRoutes() {
        return deque.size();
    }

    @Override
    public Iterator<T> getIterator() {
        return deque.iterator();
    }
}
