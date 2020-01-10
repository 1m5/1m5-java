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
package io.onemfive.core.client;

import io.onemfive.data.Envelope;
import io.onemfive.data.EventMessage;
import io.onemfive.data.ServiceCallback;
import io.onemfive.data.Subscription;

/**
 * Define the standard means of interacting with the 1M5 application when embedded.
 *
 * Never ever hold a static reference to the context or anything derived from it.
 *
 * @author objectorange
 */
public interface Client {

    /**
     * Request to 1M5 application with no reply (fire-and-forget).
     * @param envelope non-null Envelope
     * @see io.onemfive.data.Envelope
     */
    void request(Envelope envelope);

    /**
     * Request to 1M5 application with a reply using a ServiceCallback.
     * @param envelope non-null Envelope
     * @param cb non-null ServiceCallback
     * @see io.onemfive.data.Envelope
     * @see io.onemfive.data.ServiceCallback
     */
    void request(Envelope envelope, ServiceCallback cb);


    /**
     * Notify client of reply.
     * @param envelope non-null Envelope
     * @see io.onemfive.data.Envelope
     */
    void notify(Envelope envelope);

    /**
     *  The ID of the client assigned during creation.
     *  @return non-null Long
     */
    Long getId();

    /**
     * Register a ClientStatusListener so that Clients can act on Client status changes.
     * @param listener
     */
    void registerClientStatusListener(ClientStatusListener listener);

    /**
     * Subscribe to events by Type.
     * @param subscription
     */
    void subscribeToEvent(EventMessage.Type eventType, Subscription subscription);

    /**
     * Subscribe to Email events (receiving Email).
     * @param subscription
     */
    void subscribeToEmail(Subscription subscription);

}
