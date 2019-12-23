package io.onemfive.data.util;

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
 */
public final class ByteCache {

    private static final Logger LOG = Logger.getLogger(ByteCache.class.getName());

    //private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(ByteCache.class);
    private static final Map<Integer, ByteCache> _caches = new ConcurrentHashMap<Integer, ByteCache>(16);

    /**
     *  max size in bytes of each cache
     *  Set to max memory / 128, with a min of 128KB and a max of 4MB
     *
     *  @since 0.7.14
     */
    private static final int MAX_CACHE;
    static {
        long maxMemory = SystemVersion.getMaxMemory();
        MAX_CACHE = (int) Math.min(4*1024*1024l, Math.max(128*1024l, maxMemory / 128));
    }

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
        ByteCache cache = _caches.get(sz);
        if (cache == null) {
            cache = new ByteCache(cacheSize, size);
            _caches.put(sz, cache);
        }
        cache.resize(cacheSize);
        //I2PAppContext.getGlobalContext().logManager().getLog(ByteCache.class).error("ByteCache size: " + size + " max: " + cacheSize, new Exception("from"));
        return cache;
    }

    /**
     *  Clear everything (memory pressure)
     *  @since 0.7.14
     */
    public static void clearAll() {
        for (ByteCache bc : _caches.values())
            bc.clear();
        //_log.warn("WARNING: Low memory, clearing byte caches");
    }

    /** list of available and available entries */
    private volatile Queue<ByteArray> _available;
    private int _maxCached;
    private final int _entrySize;
    private volatile long _lastOverflow;

    /** do we actually want to cache? Warning - setting to false may NPE, this should be fixed or removed */
    private static final boolean _cache = true;

    /** how often do we cleanup the cache */
    private static final int CLEANUP_FREQUENCY = 33*1000;
    /** if we haven't exceeded the cache size in 2 minutes, cut our cache in half */
    private static final long EXPIRE_PERIOD = 2*60*1000;

    private ByteCache(int maxCachedEntries, int entrySize) {
        if (_cache)
            _available = new LinkedBlockingQueue<ByteArray>(maxCachedEntries);
        _maxCached = maxCachedEntries;
        _entrySize = entrySize;
        _lastOverflow = -1;
    }

    private void resize(int maxCachedEntries) {
        if (_maxCached >= maxCachedEntries) return;
        _maxCached = maxCachedEntries;
        // make a bigger one, move the cached items over
        Queue<ByteArray> newLBQ = new LinkedBlockingQueue<ByteArray>(maxCachedEntries);
        ByteArray ba;
        while ((ba = _available.poll()) != null)
            newLBQ.offer(ba);
        _available = newLBQ;
    }

    /**
     * Get the next available structure, either from the cache or a brand new one.
     * Returned ByteArray will have valid = 0 and offset = 0.
     * Returned ByteArray may or may not be zero, depends on whether
     * release(ba) or release(ba, false) was called.
     * Which is a problem, you should really specify shouldZero on acquire, not release.
     */
    public final ByteArray acquire() {
        if (_cache) {
            ByteArray rv = _available.poll();
            if (rv != null)
                return rv;
        }
        _lastOverflow = System.currentTimeMillis();
        byte data[] = new byte[_entrySize];
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
        if (_cache) {
            if (entry == null || entry.getData() == null)
                return;
            if (entry.getData().length != _entrySize) {
                LOG.warning("Bad size");
                return;
            }
            entry.setValid(0);
            entry.setOffset(0);

            if (shouldZero)
                Arrays.fill(entry.getData(), (byte)0x0);
            _available.offer(entry);
        }
    }

    /**
     *  Clear everything (memory pressure)
     */
    private void clear() {
        _available.clear();
    }
}
