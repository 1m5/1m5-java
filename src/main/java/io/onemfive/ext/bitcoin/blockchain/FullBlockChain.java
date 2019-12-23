package io.onemfive.ext.bitcoin.blockchain;

/**
 * A FullBlockChain works in conjunction with a {@link io.onemfive.ext.bitcoin.blockstore.FullBlockStore} to verify all the rules of the
 * Bitcoin system, with the downside being a very large cost in system resources. Fully verifying means all unspent
 * transaction outputs are stored.
 *
 * @author objectorange
 */
public class FullBlockChain extends BlockChain {
}
