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
package io.onemfive.monetary.btc.blockstore;

import io.onemfive.monetary.btc.blockchain.StoredBlock;
import io.onemfive.data.Hash;

/**
 * Persists StoredBlock objects to disk.
 * MemoryBlockStore is only used for testing.
 *
 * A BlockStore is a map of hashes to StoredBlock. The hash is the double digest of the Bitcoin serialization
 * of the block header, <b>not</b> the header with the extra data as well.<p>
 *
 * BlockStores are thread safe.
 *
 * @author objectorange
 */
public abstract class BlockStore {

//    protected NetworkParameters params;

    /**
     * Saves the given block header+extra data. The key isn't specified explicitly as it can be calculated from the
     * StoredBlock directly. Can throw if there is a problem with the underlying storage layer such as running out of
     * disk space.
     */
    public void put(StoredBlock block) {

    };

    /**
     * Returns the StoredBlock given a hash. The returned values block.getHash() method will be equal to the
     * parameter. If no such block is found, returns null.
     */
    public StoredBlock get(Hash hash) {
        StoredBlock sb = null;

        return sb;
    };

    /**
     * Returns the {@link StoredBlock} that represents the top of the chain of greatest total work. Note that this
     * can be arbitrarily expensive, you probably should use {@link BlockChain#getChainHead()}
     * or perhaps {@link BlockChain#getBestChainHeight()} which will run in constant time and
     * not take any heavyweight locks.
     */
    public StoredBlock getChainHead() {
        StoredBlock ch = null;

        return ch;
    };

    /**
     * Sets the {@link StoredBlock} that represents the top of the chain of greatest total work.
     */
    public void setChainHead(StoredBlock chainHead) {

    };

    /** Closes the store. */
    public void close() {

    };

    /**
     * Get the {@link NetworkParameters} of this store.
     * @return The network params.
     */
//    public NetworkParameters getParams() {
//        return params;
//    }
}
