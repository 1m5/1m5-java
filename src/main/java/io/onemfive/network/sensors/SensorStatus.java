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
package io.onemfive.network.sensors;

/**
 * Status States of a Sensor.
 *
 * @author objectorange
 */
public enum SensorStatus {
    UNREGISTERED, // 0 - Unknown/not registered yet
    // Sensor Starting Up
    NOT_INITIALIZED, // 1 - Initial state
    INITIALIZING, // 2 - Initializing Sensor's environment including configuration of Networking component
    STARTING, // 3 - Starting of Networking component
    WAITING,  // Optional 3.1 - means this sensor is waiting on a dependent sensor's status to change to STARTING, e.g. Bote waiting on I2P to begin starting up.
    // Sensor Networking
    NETWORK_WARMUP, // Optional 3.2 - means this sensor is waiting for a dependent sensor's status to change to NETWORK_CONNECTED, e.g. Bote waiting on I2P to actually connect.
    NETWORK_PORT_CONFLICT, // Optional 3.3 - means this sensor was unable to open the supplied port - likely being blocked; recommend changing ports
    NETWORK_CONNECTING, // 4 - Attempting to connect with network
    NETWORK_CONNECTED, // 5 - Network successfully connected and ready to handle requests
    NETWORK_VERIFIED, // 6 - Network has claimed to be connected (NETWORK_CONNECTED) and we have received a message from the network verifying it is
    NETWORK_STOPPING, // Network connection is hanging, e.g. unacceptable response times, begin looking at alternatives
    NETWORK_STOPPED, // Network connection failed, try another or recommend alternative
    NETWORK_BLOCKED, // Network connection being blocked.
    NETWORK_ERROR, // Error in Network; handle within Sensor if possible yet make Sensor Service aware of likely service degradation.
    // Sensor Pausing (Not Yet Supported In Any Sensors)
    PAUSING, // Queueing up requests both inbound and outbound waiting for pre-pausing requests to complete.
    PAUSED, // All pre-pausing requests completed.
    UNPAUSING, // Unblocking queued requests to allow them to continue on and not queueing further requests.
    // Sensor Shutdown
    SHUTTING_DOWN, // Shutdown imminent - not clean, process likely getting killed - perform the minimum ASAP
    GRACEFULLY_SHUTTING_DOWN, // Ideal clean teardown
    SHUTDOWN, // Was teardown forcefully - expect potential file / state corruption
    GRACEFULLY_SHUTDOWN, // Shutdown was graceful - safe to assume no file / state corruption
    // Sensor Restarting
    RESTARTING, // Short for GRACEFULLY_SHUTTING_DOWN then STARTING back up.
    // Sensor Error
    ERROR // Likely need of Sensor restart
}
