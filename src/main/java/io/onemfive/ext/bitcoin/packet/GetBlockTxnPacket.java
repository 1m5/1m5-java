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
package io.onemfive.ext.bitcoin.packet;

/**
 * A message containing a serialized BlockTransactionsRequest message and pchCommand == "getblocktxn".
 *
 * Upon receipt of a properly-formatted {@link GetBlockTxnPacket}, nodes which recently provided
 * the sender of such a message a {@link CmpctBlockPacket} for the block hash identified in this
 * message MUST respond with an appropriate {@link BlockTxnPacket}.
 *
 * Such a {@link BlockTxnPacket} MUST contain exactly and only each transaction which is present
 * in the appropriate {@link io.onemfive.ext.bitcoin.blockchain.Block} at the index specified in
 * the {@link GetBlockTxnPacket} indexes list, in the order requested.
 *
 * This message is only supported by protocol version >= 70014
 *
 * @author objectorange
 */
public class GetBlockTxnPacket extends BitcoinPacket {
}
