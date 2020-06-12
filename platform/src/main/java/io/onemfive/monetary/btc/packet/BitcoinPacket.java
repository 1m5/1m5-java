package io.onemfive.monetary.btc.packet;

import io.onemfive.data.BaseMessage;

/**
 * https://en.bitcoin.it/wiki/Protocol_specification
 *
 */
public abstract class BitcoinPacket extends BaseMessage {

    protected byte[] payload;
    protected BitcoinPacket parent;

}
