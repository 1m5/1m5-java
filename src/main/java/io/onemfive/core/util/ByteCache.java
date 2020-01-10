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

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.util.data.ByteArray;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Cache the objects frequently used to reduce memory churn.  The ByteArray
 * should be held onto as long as the  data referenced in it is needed.
 *
 * For small arrays where the management of valid bytes in ByteArray
 * and prezeroing isn't required, use SimpleByteArray instead.
 *
 * Heap size control - survey of usage:
 *
 *  <pre>
 Size	Max	MaxMem	From

 1K	32	32K	tunnel TrivialPreprocessor
 1K	512	512K	tunnel FragmentHandler
 1K	512	512K	I2NP TunnelDataMessage
 1K	512	512K	tunnel FragmentedMessage

 1730	128	216K	streaming MessageOutputStream

 2K	64	128K	UDP IMS

 4K	32	128K	I2PTunnelRunner

 8K	8	64K	I2PTunnel HTTPResponseOutputStream

 16K	16	256K	I2PSnark

 32K	4	128K	SAM StreamSession
 32K	10	320K	SAM v2StreamSession
 32K	64	2M	UDP OMS
 32K	128	4M	streaming MessageInputStream

 36K	64	2.25M	streaming PacketQueue

 40K	8	320K	DataHelper decompress

 64K	64	4M	UDP MessageReceiver - disabled in 0.7.14
 *  </pre>
 *
 */
public final class ByteCache {

    private static final Logger LOG = Logger.getLogger(ByteCache.class.getName());

    private static final Map<Integer, ByteCache> caches = new ConcurrentHashMap<Integer, ByteCache>(16);

    /**
     *  max size in bytes of each cache
     *  Set to max memory / 128, with a min of 128KB and a max of 4MB
     */
    private static final int MAX_CACHE;

    static {
        long maxMemory = SystemVersion.getMaxMemory();
        MAX_CACHE = (int) Math.min(4*1024*1024l, Math.max(128*1024l, maxMemory / 128));
    }

    /** do we actually want to cache? Warning - setting to false may NPE, this should be fixed or removed */
    private static final boolean cache = true;

    /** how often do we cleanup the cache */
    private static final int CLEANUP_FREQUENCY = 33*1000;
    /** if we haven't exceeded the cache size in 2 minutes, cut our cache in half */
    private static final long EXPIRE_PERIOD = 2*60*1000;

    /** list of available and available entries */
    private volatile Queue<ByteArray> available;
    private int maxCached;
    private final int entrySize;
    private volatile long lastOverflow;

    /**
     * Get a cache responsible for objects of the given size.
     * Warning, if you store the result in a static field, the cleaners will
     * not operate after a restart on Android, as the old context's SimpleTimer2 will have shut down.
     * TODO tie this to the context or clean up all calls.
     *
     * @param cacheSize how large we want the cache to grow
     *                  (number of objects, NOT memory size)
     *                  before discarding released objects.
     *                  Since 0.7.14, a limit of 1MB / size is enforced
     *                  for the typical 128MB max memory JVM
     * @param size how large should the objects cached be?
     */
    public static ByteCache getInstance(int cacheSize, int size) {
        if (cacheSize * size > MAX_CACHE)
            cacheSize = MAX_CACHE / size;
        Integer sz = Integer.valueOf(size);
        ByteCache cache = caches.get(sz);
        if (cache == null) {
            cache = new ByteCache(cacheSize, size);
            caches.put(sz, cache);
        }
        cache.resize(cacheSize);
        //I2PAppContext.getGlobalContext().logManager().getLog(ByteCache.class).error("ByteCache size: " + size + " max: " + cacheSize, new Exception("from"));
        return cache;
    }

    private ByteCache(int maxCachedEntries, int entrySize) {
        if (cache)
            available = new LinkedBlockingQueue<ByteArray>(maxCachedEntries);
        maxCached = maxCachedEntries;
        this.entrySize = entrySize;
        lastOverflow = -1;
        SimpleTimer.getInstance().addPeriodicEvent(new Cleanup(), CLEANUP_FREQUENCY + (entrySize % 777));   //stagger
        OneMFiveAppContext.getInstance().statManager().createRateStat("byteCache.memory." + entrySize, "Memory usage (B)", "Router", new long[] { 10*60*1000 });
    }

    /**
     *  Clear everything (memory pressure)
     */
    public static void clearAll() {
        for (ByteCache bc : caches.values())
            bc.clear();
        LOG.warning("Low memory, clearing byte caches");
    }

    private void resize(int maxCachedEntries) {
        if (maxCached >= maxCachedEntries) return;
        maxCached = maxCachedEntries;
        // make a bigger one, move the cached items over
        Queue<ByteArray> newLBQ = new LinkedBlockingQueue<ByteArray>(maxCachedEntries);
        ByteArray ba;
        while ((ba = available.poll()) != null)
            newLBQ.offer(ba);
        available = newLBQ;
    }

    /**
     * Get the next available structure, either from the cache or a brand new one.
     * Returned ByteArray will have valid = 0 and offset = 0.
     * Returned ByteArray may or may not be zero, depends on whether
     * release(ba) or release(ba, false) was called.
     * Which is a problem, you should really specify shouldZero on acquire, not release.
     */
    public final ByteArray acquire() {
        if (cache) {
            ByteArray rv = available.poll();
            if (rv != null)
                return rv;
        }
        lastOverflow = System.currentTimeMillis();
        byte data[] = new byte[entrySize];
        ByteArray rv = new ByteArray(data);
        rv.setValid(0);
        //rv.setOffset(0);
        return rv;
    }

    /**
     * Put this structure back onto the available cache for reuse
     *
     */
    public final void release(ByteArray entry) {
        release(entry, true);
    }

    public final void release(ByteArray entry, boolean shouldZero) {
        if (cache) {
            if (entry == null || entry.getData() == null)
                return;
            if (entry.getData().length != entrySize) {
                LOG.warning("Bad size");
                return;
            }
            entry.setValid(0);
            entry.setOffset(0);

            if (shouldZero)
                Arrays.fill(entry.getData(), (byte)0x0);
            available.offer(entry);
        }
    }

    /**
     *  Clear everything (memory pressure)
     */
    private void clear() {
        available.clear();
    }

    private class Cleanup implements TimerEvent {

        public void timeReached() {
            OneMFiveAppContext.getInstance().statManager().addRateData("byteCache.memory." + entrySize, entrySize * available.size(), 0);
            if (System.currentTimeMillis() - lastOverflow > EXPIRE_PERIOD) {
                // we haven't exceeded the cache size in a few minutes, so lets
                // shrink the cache
                int toRemove = available.size() / 2;
                for (int i = 0; i < toRemove; i++)
                    available.poll();
                //if ( (toRemove > 0) && (_log.shouldLog(Log.DEBUG)) )
                //    _log.debug("Removing " + toRemove + " cached entries of size " + _entrySize);
            }
        }

        @Override
        public String toString() {
            return "Cleaner for " + entrySize + " byte cache";
        }
    }
}
