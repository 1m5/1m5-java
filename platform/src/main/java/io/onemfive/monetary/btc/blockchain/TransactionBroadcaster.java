package io.onemfive.monetary.btc.blockchain;

/**
 * TODO: Add Description
 *
 */
public interface TransactionBroadcaster {
    TransactionBroadcast broadcastTransaction(final Transaction tx);
}
