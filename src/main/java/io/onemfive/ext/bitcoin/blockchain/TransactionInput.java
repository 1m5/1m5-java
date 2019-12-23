package io.onemfive.ext.bitcoin.blockchain;

import io.onemfive.ext.bitcoin.Satoshi;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class TransactionInput {

    private Satoshi value;
    private byte[] script;

    public TransactionInput(byte[] script) {
        this.script = script;
        this.value = new Satoshi(0);
    }

    public TransactionInput(byte[] script, long value) {
        this.script = script;
        this.value = new Satoshi(value);
    }
}
