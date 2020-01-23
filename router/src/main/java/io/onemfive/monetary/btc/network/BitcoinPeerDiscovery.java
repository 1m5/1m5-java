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
package io.onemfive.monetary.btc.network;

import io.onemfive.monetary.btc.BitcoinContext;
import io.onemfive.monetary.btc.blockchain.BlockChain;
import io.onemfive.monetary.btc.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class BitcoinPeerDiscovery {

    private BitcoinContext context;
    private BlockChain chain;
    private List<Wallet> wallets;

    /**
     * With no blockchain provided, height will appear at zero.
     * Good for exploring the network without downloading blocks.
     * @param context
     */
    public BitcoinPeerDiscovery(BitcoinContext context) {
        this(context, null);
    }

    public BitcoinPeerDiscovery(BitcoinContext context, BlockChain chain) {
        this.context = context;
        this.chain = chain;
        this.wallets = new ArrayList<>();
    }


}