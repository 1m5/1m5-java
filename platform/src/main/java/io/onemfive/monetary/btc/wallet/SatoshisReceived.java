package io.onemfive.monetary.btc.wallet;

import io.onemfive.monetary.btc.Satoshi;
import io.onemfive.monetary.btc.blockchain.Transaction;

/**
 * TODO: Add Description
 *
 */
public interface SatoshisReceived {
    void onSatoshisReceived(Wallet wallet, Transaction tx, Satoshi prevBalance, Satoshi newBalance);
}
