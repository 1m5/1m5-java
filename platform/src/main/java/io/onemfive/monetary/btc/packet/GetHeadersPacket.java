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
 * Return a headers packet containing the headers of blocks starting right after the last
 * known hash in the block locator object, up to hash_stop or 2000 blocks, whichever comes first.
 *
 * To receive the next block headers, one needs to issue {@link GetHeadersPacket} again with a
 * new block locator object.
 *
 * Keep in mind that some clients may provide headers of blocks which are invalid if the
 * block locator object contains a hash on the invalid branch.
 *
 * For the block locator object in this packet, the same rules apply as for the {@link GetBlocksPacket}.
 *
 * @author objectorange
 */
public class GetHeadersPacket extends BitcoinPacket {
}
