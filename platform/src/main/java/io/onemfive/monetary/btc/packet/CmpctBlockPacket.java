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
 * A message containing a serialized HeaderAndShortIDs message and pchCommand == "cmpctblock".
 *
 * Upon receipt of a {@link CmpctBlockPacket} after sending a {@link SendCmpctPacket},
 * nodes SHOULD calculate the short transaction ID for each unconfirmed transaction they
 * have available (ie in their mempool) and compare each to each short transaction ID in the cmpctblock message.
 *
 * After finding already-available transactions, nodes which do not have all transactions available to
 * reconstruct the full block SHOULD request the missing transactions using a {@link GetBlockTxnPacket}.
 *
 * A node MUST NOT send a {@link CmpctBlockPacket} unless they are able to respond to
 * a {@link GetBlockTxnPacket} which requests every transaction in the block.
 *
 * A node MUST NOT send a {@link CmpctBlockPacket} without having validated that the header properly
 * commits to each transaction in the block, and properly builds on top of the existing chain with
 * a valid proof-of-work.
 *
 * A node MAY send a {@link CmpctBlockPacket} before validating that each transaction in the block
 * validly spends existing UTXO set entries.
 *
 * This packet is only supported by protocol version >= 70014
 *
 * @author objectorange
 */
public class CmpctBlockPacket extends BitcoinPacket {
}
