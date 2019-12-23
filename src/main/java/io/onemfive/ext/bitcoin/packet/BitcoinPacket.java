package io.onemfive.ext.bitcoin.packet;

import io.onemfive.data.BaseMessage;

/**
 * https://en.bitcoin.it/wiki/Protocol_specification
 *
 * @author objectorange
 */
public abstract class BitcoinPacket extends BaseMessage {

    protected byte[] payload;
    protected BitcoinPacket parent;

}
