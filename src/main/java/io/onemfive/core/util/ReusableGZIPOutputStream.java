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

import io.onemfive.core.util.data.DataHelper;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Provide a cache of reusable GZIP streams, each handling up to 40 KB output without
 * expansion.
 *
 * This compresses to memory only. Retrieve the compressed data with getData().
 * There is no facility to compress to an output stream.
 *
 * Do NOT use this for compression of unlimited-size data, as it will
 * expand, but never release, the BAOS memory buffer.
 */
public class ReusableGZIPOutputStream extends ResettableGZIPOutputStream {
    // Apache Harmony 5.0M13 Deflater doesn't work after reset()
    // Neither does Android
    // attempt to fix #1915
    //private static final boolean ENABLE_CACHING = !(SystemVersion.isApache() ||
    //                                                SystemVersion.isAndroid());
    private static final boolean ENABLE_CACHING = false;
    private static final LinkedBlockingQueue<ReusableGZIPOutputStream> available;
    static {
        if (ENABLE_CACHING)
            available = new LinkedBlockingQueue<ReusableGZIPOutputStream>(16);
        else
            available = null;
    }

    /**
     * Pull a cached instance
     */
    public static ReusableGZIPOutputStream acquire() {
        ReusableGZIPOutputStream rv = null;
        if (ENABLE_CACHING)
            rv = available.poll();
        if (rv == null) {
            rv = new ReusableGZIPOutputStream();
        }
        return rv;
    }

    /**
     * Release an instance back into the cache (this will discard any
     * state)
     */
    public static void release(ReusableGZIPOutputStream out) {
        out.reset();
        if (ENABLE_CACHING)
            available.offer(out);
    }

    private final ByteArrayOutputStream _buffer;

    private ReusableGZIPOutputStream() {
        super(new ByteArrayOutputStream(DataHelper.MAX_UNCOMPRESSED));
        _buffer = (ByteArrayOutputStream)out;
    }

    /** clear the data so we can init again afresh */
    @Override
    public void reset() {
        super.reset();
        _buffer.reset();
        def.setLevel(Deflater.BEST_COMPRESSION);
    }

    public void setLevel(int level) {
        def.setLevel(level);
    }

    /** pull the contents of the stream written */
    public byte[] getData() { return _buffer.toByteArray(); }

    /**
     *  Clear the cache.
     */
    public static void clearCache() {
        if (available != null)
            available.clear();
    }

}
