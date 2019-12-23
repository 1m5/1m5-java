package io.onemfive.ext.bitcoin.packet;

/**
 * The headers packet returns block headers in response to a {@link GetHeadersPacket}.
 *
 * Note that the block headers in this packet include a transaction count
 * (a var_int, so there can be more than 81 bytes per header) as opposed to
 * the block headers that are hashed by miners.
 *
 * @author objectorange
 */
public class HeadersPacket extends BitcoinPacket {
}
