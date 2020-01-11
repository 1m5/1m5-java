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
 * The block packet is sent in response to a {@link GetDataPacket} which requests
 * transaction information from a block hash.
 *
 * The SHA256 hash that identifies each {@link io.onemfive.ext.bitcoin.blockchain.Block}
 * (and which must have a run of 0 bits) is calculated from the first 6 fields of
 * this structure (version, prev_block, merkle_root, timestamp, bits, nonce, and
 * standard SHA256 padding, making two 64-byte chunks in all) and not from the
 * complete block.
 *
 * To calculate the hash, only two chunks need to be processed by the SHA256 algorithm.
 *
 * Since the nonce field is in the second chunk, the first chunk stays constant during
 * mining and therefore only the second chunk needs to be processed.
 *
 * However, a Bitcoin hash is the hash of the hash, so two SHA256 rounds are needed
 * for each mining iteration.
 *
 * @author objectorange
 */
public class BlockPacket extends BitcoinPacket {
}
