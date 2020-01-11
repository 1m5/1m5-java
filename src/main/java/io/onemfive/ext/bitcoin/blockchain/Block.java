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
package io.onemfive.ext.bitcoin.blockchain;

import io.onemfive.data.Hash;

/**
 * A block of transactions
 *
 * @author objectorange
 */
public class Block {

    /**
     * Max block size in bytes as a method to prevent DDoS attacks by creating a large enough valid block resulting
     * in the possibility of crashing a large number of nodes, potentially the whole network.
     */
    public static final int MAX_BLOCK_SIZE = 1000000; // 1mb
    /**
     * A "sigop" is a signature verification operation. Because they're expensive we also impose a separate limit on
     * the number in a block to prevent somebody mining a huge block that has way more sigops than normal, so is very
     * expensive/slow to verify.
     */
    public static final int MAX_BLOCK_SIGOPS = MAX_BLOCK_SIZE / 50;

    private long version;
    private Hash prevBlockHash;
    private Hash merkleRoot;
    private long time;
    private long difficultyTarget = 0x1d07fff8L;
    private long nonce;

    public Block(int version) {
        this.version = version;
        this.time = System.currentTimeMillis();
        this.prevBlockHash = new Hash("",Hash.Algorithm.SHA256);
    }
}
