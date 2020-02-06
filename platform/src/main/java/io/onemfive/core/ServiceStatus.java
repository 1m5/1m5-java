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
package io.onemfive.core;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public enum ServiceStatus {
    // Service Starting Up
    NOT_INITIALIZED, // Initial state
    INITIALIZING, // Initializing service configuration
    WAITING, // Waiting on a dependent Service status to go to RUNNING
    STARTING, // Starting Service
    RUNNING, // Service is running normally
    VERIFIED, // Service has been verified operating normally by receiving a message from it
    PARTIALLY_RUNNING, // Service is running normally although not everything is running but it's expected to be normal
    DEGRADED_RUNNING, // Service is running but in a degraded manner; likely no need for action, will hopefully come back to Running
    BLOCKED, // Service is being blocked from usage
    UNSTABLE, // Service is running but there could be issues; likely need to restart
    // Service Pausing (Not Yet Supported In Any Service)
    PAUSING, // Service will begin queueing all new requests while in-process requests will be completed
    PAUSED, // Service is queueing new requests and pre-pausing requests have completed
    UNPAUSING, // Service has stopped queueing new requests and is starting to resume normal operations
    // Service Shutdown
    SHUTTING_DOWN, // Service teardown imminent - not clean, process likely getting killed - perform the minimum ASAP
    GRACEFULLY_SHUTTING_DOWN, // Ideal clean teardown
    SHUTDOWN, // Was teardown forcefully - expect potential file / state corruption
    GRACEFULLY_SHUTDOWN, // Shutdown was graceful - safe to assume no file / state corruption
    // Restarting
    RESTARTING, // Short for GRACEFULLY_SHUTTING_DOWN followed by INITIALIZING on up
    // Unavailable
    UNAVAILABLE, // No Network available but not through blocking, more likely either not installed or not turned on
    // Service Error
    ERROR // Likely need of Service restart
}
