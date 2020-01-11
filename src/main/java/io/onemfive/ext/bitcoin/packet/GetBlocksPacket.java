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
 * Return an {@link InvPacket} containing the list of {@link io.onemfive.ext.bitcoin.blockchain.Block}
 * starting right after the last known hash in the block locator object, up to hash_stop or 500 blocks,
 * whichever comes first.
 *
 * The locator hashes are processed by a node in the order as they appear in the message.
 * If a block hash is found in the node's main chain, the list of its children is returned
 * back via the {@link InvPacket} and the remaining locators are ignored, no matter if the
 * requested limit was reached, or not.
 *
 * To receive the next blocks hashes, one needs to issue {@link GetBlocksPacket} again with
 * a new block locator object.
 *
 * Keep in mind that some clients may provide blocks which are invalid if the block locator
 * object contains a hash on the invalid branch.
 *
 * Note that it is allowed to send in fewer known hashes down to a minimum of just one hash.
 * However, the purpose of the block locator object is to detect a wrong branch in the
 * caller's main chain.
 *
 * If the peer detects that you are off the main chain, it will send in block hashes which are
 * earlier than your last known block.
 *
 * So if you just send in your last known hash and it is off the main chain, the peer
 * starts over at block #1.
 *
 * @author objectorange
 */
public class GetBlocksPacket extends BitcoinPacket {
}
