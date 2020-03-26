/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.network.sensors.tor.local.control;

/**
 * Abstract interface whose methods are invoked when Tor sends us an event.
 *
 * @see TORControlConnection#setEventHandler
 * @see TORControlConnection#setEvents
 */
public interface EventHandler {
    /**
     * Invoked when a circuit's status has changed.
     * Possible values for <b>status</b> are:
     * <ul>
     *   <li>"LAUNCHED" :  circuit ID assigned to new circuit</li>
     *   <li>"BUILT"    :  all hops finished, can now accept streams</li>
     *   <li>"EXTENDED" :  one more hop has been completed</li>
     *   <li>"FAILED"   :  circuit closed (was not built)</li>
     *   <li>"CLOSED"   :  circuit closed (was built)</li>
     *	</ul>
     *
     * <b>circID</b> is the alphanumeric identifier of the affected circuit,
     * and <b>path</b> is a comma-separated list of alphanumeric ServerIDs.
     */
    void circuitStatus(String status, String circID, String path);
    /**
     * Invoked when a stream's status has changed.
     * Possible values for <b>status</b> are:
     * <ul>
     *   <li>"NEW"         :  New request to connect</li>
     *   <li>"NEWRESOLVE"  :  New request to resolve an address</li>
     *   <li>"SENTCONNECT" :  Sent a connect cell along a circuit</li>
     *   <li>"SENTRESOLVE" :  Sent a resolve cell along a circuit</li>
     *   <li>"SUCCEEDED"   :  Received a reply; stream established</li>
     *   <li>"FAILED"      :  Stream failed and not retriable.</li>
     *   <li>"CLOSED"      :  Stream closed</li>
     *   <li>"DETACHED"    :  Detached from circuit; still retriable.</li>
     *	</ul>
     *
     * <b>streamID</b> is the alphanumeric identifier of the affected stream,
     * and its <b>target</b> is specified as address:port.
     */
    void streamStatus(String status, String streamID, String target);
    /**
     * Invoked when the status of a connection to an OR has changed.
     * Possible values for <b>status</b> are ["LAUNCHED" | "CONNECTED" | "FAILED" | "CLOSED"].
     * <b>orName</b> is the alphanumeric identifier of the OR affected.
     */
    void orConnStatus(String status, String orName);
    /**
     * Invoked once per second. <b>read</b> and <b>written</b> are
     * the number of bytes read and written, respectively, in
     * the last second.
     */
    void bandwidthUsed(long read, long written);
    /**
     * Invoked whenever Tor learns about new ORs.  The <b>orList</b> object
     * contains the alphanumeric ServerIDs associated with the new ORs.
     */
    void newDescriptors(java.util.List<String> orList);
    /**
     * Invoked when Tor logs a message.
     * <b>severity</b> is one of ["DEBUG" | "INFO" | "NOTICE" | "WARN" | "ERR"],
     * and <b>msg</b> is the message string.
     */
    void message(String severity, String msg);

    /**
     * Invoked when Tor has information about a hidden service.
     *
     * @param action is the message action
     * @param msg    is the message string.
     */
    void hiddenServiceEvent(String action, String msg);

    /**
     * Invoked when Tor reports an error regarding a hidden service.
     *
     * @param reason [BAD_DESC,QUERY_REJECTED,UPLOAD_REJECTED,NOT_FOUND,UNEXPECTED,QUERY_NO_HSDIR,
     *               NO_REASON]
     * @param msg
     */
    void hiddenServiceFailedEvent(String reason, String msg);

    /**
     * Invoked when Tor has a hidden service descriptor ready.
     *
     * @param descriptorId
     * @param descriptor
     * @param msg
     */
    void hiddenServiceDescriptor(String descriptorId, String descriptor, String msg);

    /**
     * Invoked when an unspecified message is received. <type> is the message type,
     * and <msg> is the message string.
     */
    void unrecognized(String type, String msg);

    /**
     * Invoked when the Tor Control Connection takes longer than one minute to respond.
     */
    void timeout();
}
