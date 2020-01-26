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
package io.onemfive.monetary.btc.packet;

/**
 * The value represents a minimal fee and is expressed in satoshis per kilobyte.
 *
 * The payload is always 8 bytes long and it encodes 64 bit integer value (LSB / little endian) of feerate.
 *
 * Upon receipt of a {@link FeeFilterPacket}, the node will be permitted, but not required, to
 * filter transaction invs for transactions that fall below the feerate provided in the
 * {@link FeeFilterPacket} interpreted as satoshis per kilobyte.
 *
 * The fee filter is additive with a bloom filter for transactions so if an SPV client were to load
 * a bloom filter and send a {@link FeeFilterPacket}, transactions would only be relayed if they passed both filters.
 *
 * Inv's generated from a mempool message are also subject to a fee filter if it exists.
 *
 * Feature discovery is enabled by checking protocol version >= 70013
 *
 * @author objectorange
 */
public class FeeFilterPacket extends BitcoinPacket {
}
