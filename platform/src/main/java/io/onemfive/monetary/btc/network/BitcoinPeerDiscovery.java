package io.onemfive.monetary.btc.network;

import io.onemfive.monetary.btc.BitcoinContext;
import io.onemfive.monetary.btc.blockchain.BlockChain;
import io.onemfive.monetary.btc.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add Description
 *
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
