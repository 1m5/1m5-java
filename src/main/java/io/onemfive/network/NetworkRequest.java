package io.onemfive.network;

import io.onemfive.data.ServiceMessage;
import io.onemfive.data.DID;

/**
 * Request to use a Network for external communications.
 */
public class NetworkRequest extends ServiceMessage {

    public static int DESTINATION_PEER_REQUIRED = 1;
    public static int DESTINATION_PEER_WRONG_NETWORK = 2;
    public static int NO_CONTENT = 3;
    public static int DESTINATION_PEER_NOT_FOUND = 4;
    public static int SENDING_FAILED = 5;

    public DID origination;
    public DID destination;
    public String content;

}
