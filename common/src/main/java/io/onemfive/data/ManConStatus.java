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
package io.onemfive.data;

public final class ManConStatus {
    // Max Supported ManCon is the highest supported ManCon by Sensor Manager determination by looking at what Sensors were registered and are active.
    // Current default setting of HIGH is based on registering Tor, I2P, and Bluetooth by default.
    public static ManCon MAX_SUPPORTED_MANCON = ManCon.EXTREME;
    // Max Available ManCon is the current level of ManCon that can be supported determined by Sensor Manager
    // changing in real-time based on network connectivity and peer discovery.
    // Initial setting is NONE until sensors come on line, connect, and discover peers. This is managed by the Sensor Manager.
    public static ManCon MAX_AVAILABLE_MANCON = ManCon.NONE;
    // Min Required ManCon is set by end users or system admins for daemons to indicate the minimum ManCon to use for current communications.
    // TODO: Load this from a configuration
    public static ManCon MIN_REQUIRED_MANCON = ManCon.HIGH;

}
