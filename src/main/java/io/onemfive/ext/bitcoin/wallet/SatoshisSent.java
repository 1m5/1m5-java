package io.onemfive.ext.bitcoin.wallet;

import io.onemfive.ext.bitcoin.Satoshi;
import io.onemfive.ext.bitcoin.blockchain.Transaction;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public interface SatoshisSent {
    void onSatoshisSent(Wallet wallet, Transaction tx, Satoshi prevBalance, Satoshi newBalance);
}
