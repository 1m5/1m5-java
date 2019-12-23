package io.onemfive.data.currency;

/**
 * TODO: Add Description
 *
 * @author objectorange
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
