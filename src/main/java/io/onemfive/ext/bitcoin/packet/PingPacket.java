package io.onemfive.ext.bitcoin.packet;

/**
 * The ping message is sent primarily to confirm that the TCP/IP connection is still valid.
 *
 * An error in transmission is presumed to be a closed connection and the address is removed as a current peer.
 *
 * @author objectorange
 */
public class PingPacket extends BitcoinPacket {
}
