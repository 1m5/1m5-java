package io.onemfive.monetary;

/**
 * TODO: Add Description
 *
 */
public interface Coin {
    boolean limitedSupply();
    long maxSupply();
    /**
     * Returns the number of "smallest units" of this currency's value.
     * For Bitcoin, this is the number of satoshis.
     */
    long value();
    String symbol();
}
