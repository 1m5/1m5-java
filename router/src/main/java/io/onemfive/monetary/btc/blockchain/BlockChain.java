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
package io.onemfive.monetary.btc.blockchain;

import io.onemfive.monetary.btc.config.BitcoinConfig;
import io.onemfive.monetary.btc.blockstore.BlockStore;

import java.util.logging.Logger;

/**
 * Contains a series of {@link Block} instances chained together by verifying the rules
 * defined in the {@link BitcoinConfig}.
 *
 * @author objectorange
 */
public abstract class BlockChain {

    private static Logger LOG = Logger.getLogger(BlockChain.class.getName());

    protected BitcoinConfig config;

    protected BlockStore blockStore;

    /**
     * The end of the chain.
     * It can change if a new set of blocks is received that results in a chain of greater work.
     * When that happens, a reorganization is triggered potentially invalidating transactions in the wallet
     * if accepted as the new head.
     */
    protected StoredBlock chainHead;


}
