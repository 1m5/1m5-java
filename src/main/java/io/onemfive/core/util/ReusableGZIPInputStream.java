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

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Provide a cache of reusable GZIP unzipper streams.
 * This provides stream output only, and therefore can handle unlimited size.
 */
public class ReusableGZIPInputStream extends ResettableGZIPInputStream {
    // Apache Harmony 5.0M13 Deflater doesn't work after reset()
    // Neither does Android
    private static final boolean ENABLE_CACHING = !(SystemVersion.isApache() ||
            SystemVersion.isAndroid());
    private static final LinkedBlockingQueue<ReusableGZIPInputStream> available;

    static {
        if (ENABLE_CACHING)
            available = new LinkedBlockingQueue<ReusableGZIPInputStream>(8);
        else
            available = null;
    }

    /**
     * Pull a cached instance
     */
    public static ReusableGZIPInputStream acquire() {
        ReusableGZIPInputStream rv = null;
        // Apache Harmony 5.0M13 Deflater doesn't work after reset()
        if (ENABLE_CACHING)
            rv = available.poll();
        if (rv == null) {
            rv = new ReusableGZIPInputStream();
        }
        return rv;
    }
    /**
     * Release an instance back into the cache (this will reset the
     * state)
     */
    public static void release(ReusableGZIPInputStream released) {
        if (ENABLE_CACHING)
            available.offer(released);
    }

    private ReusableGZIPInputStream() { super(); }

    /**
     *  Clear the cache.
     */
    public static void clearCache() {
        if (available != null)
            available.clear();
    }

}
