package io.onemfive.ext.bitcoin;

import io.onemfive.ext.bitcoin.config.BitcoinConfig;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class BitcoinContext {

    private BitcoinConfig config;
    private Satoshi feePerKb;
    private boolean ensureMinRequiredFee;

    public BitcoinConfig getConfig() {
        return config;
    }

    public void setConfig(BitcoinConfig config) {
        this.config = config;
    }

    public Satoshi getFeePerKb() {
        return feePerKb;
    }

    public void setFeePerKb(Satoshi feePerKb) {
        this.feePerKb = feePerKb;
    }

    public boolean isEnsureMinRequiredFee() {
        return ensureMinRequiredFee;
    }

    public void setEnsureMinRequiredFee(boolean ensureMinRequiredFee) {
        this.ensureMinRequiredFee = ensureMinRequiredFee;
    }
}
