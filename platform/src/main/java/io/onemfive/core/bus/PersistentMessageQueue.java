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
package io.onemfive.core.bus;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * Provides persistence to ArrayBlockingQueue
 *
 * TODO: Implement and place in Message Channel
 *
 * @author objectorange
 */
final class PersistentMessageQueue<E> extends ArrayBlockingQueue<E> {

    private final Logger LOG = Logger.getLogger(PersistentMessageQueue.class.getName());

    public PersistentMessageQueue(int capacity) {
        super(capacity);
    }

    public PersistentMessageQueue(int capacity, boolean fair) {
        super(capacity, fair);
    }

    public PersistentMessageQueue(int capacity, boolean fair, Collection<? extends E> c) {
        super(capacity, fair, c);
    }

    @Override
    public boolean add(E e) {
        boolean success = super.add(e);
        if(success) {
            // persist

        }
        return success;
    }

    @Override
    public E take() throws InterruptedException {
        E obj = super.take();
            // load

        return obj;
    }

    @Override
    public boolean remove(Object o) {
        boolean success = super.remove(o);
        if(success) {
            // remove

        }
        return success;
    }
}
