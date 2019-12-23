package io.onemfive.ext.bitcoin.packet;

/**
 * Sent in response to a {@link PingPacket}.
 *
 * In modern protocol versions, a {@link PongPacket} is generated using a nonce included in the {@link PingPacket}.
 *
 * @author objectorange
 */
public class PongPacket extends BitcoinPacket {
}
