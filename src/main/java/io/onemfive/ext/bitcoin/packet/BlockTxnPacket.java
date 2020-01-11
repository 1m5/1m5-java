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
 * A message containing a serialized BlockTransactions message and pchCommand == "blocktxn".
 *
 * Upon receipt of a properly-formatted requested {@link BlockTxnPacket}, nodes SHOULD attempt
 * to reconstruct the full block by:
 *
 *      Taking the prefilledtxn transactions from the original cmpctblock and placing them in the marked positions.
 *
 *      For each short transaction ID from the original cmpctblock, in order, find the corresponding transaction
 *      either from the blocktxn message or from other sources and place it in the first available position
 *      in the block.
 *
 *      Once the block has been reconstructed, it shall be processed as normal, keeping in mind that short
 *      transaction IDs are expected to occasionally collide, and that nodes MUST NOT be penalized for such
 *      collisions, wherever they appear.
 *
 * @author objectorange
 */
public class BlockTxnPacket extends BitcoinPacket {
}
