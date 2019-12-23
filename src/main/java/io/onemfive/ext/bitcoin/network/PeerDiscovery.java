package io.onemfive.ext.bitcoin.network;

import io.onemfive.ext.bitcoin.Context;
import io.onemfive.ext.bitcoin.blockchain.BlockChain;
import io.onemfive.ext.bitcoin.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class PeerDiscovery {

    private Context context;
    private BlockChain chain;
    private List<Wallet> wallets;

    /**
     * With no blockchain provided, height will appear at zero.
     * Good for exploring the network without downloading blocks.
     * @param context
     */
    public PeerDiscovery(Context context) {
        this(context, null);
    }

    public PeerDiscovery(Context context, BlockChain chain) {
        this.context = context;
        this.chain = chain;
        this.wallets = new ArrayList<>();
    }


}
