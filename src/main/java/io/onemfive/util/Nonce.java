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
package io.onemfive.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Enforcing a nonce long.
 *
 * @author objectorange
 */
public class Nonce {

    private static Logger LOG = Logger.getLogger(Nonce.class.getName());

    private List<Long> nonceList = new ArrayList<>(); // Envelope id
    private int maxNonceListSize; // to prevent the nonce list from consuming excess memory
    private int prunePercentSize; // size in percent to prune once maxNonceListSize reached

    public Nonce() {
        this(new ArrayList<>(), 1000000, 10); // 1 million (8Mb)
    }

    public Nonce(int maxNonceListSize) {
        this(new ArrayList<>(), maxNonceListSize, 10);
    }

    public Nonce(int maxNonceListSize, int prunePercentSize) {
        this(new ArrayList<>(), maxNonceListSize, prunePercentSize);
    }

    /**
     * Supports setting nonceList from prior persistence.
     * @param nonceList
     * @param maxNonceListSize
     */
    public Nonce(List<Long> nonceList, int maxNonceListSize, int prunePercentSize) {
        this.nonceList  = nonceList;
        this.maxNonceListSize = maxNonceListSize;
        if(prunePercentSize < 0) prunePercentSize = 0;
        else if(prunePercentSize > 100) prunePercentSize = 100;
        this.prunePercentSize = prunePercentSize;
    }

    public boolean continueOn(long id) {
        pruneNonceList();
        if(nonceList.contains(id)) {
            return false;
        } else {
            nonceList.add(id);
        }
        return true;
    }

    private void pruneNonceList() {
        LOG.info(nonceList.size()+" ids in nonce list. Max size = "+maxNonceListSize);
        if(nonceList.size() > maxNonceListSize) {
            LOG.info("Pruning nonce list by "+prunePercentSize+"%...");
            if(prunePercentSize==100) {
                nonceList.clear();
                LOG.info("Nonce list cleared.");
            } else {
                int numberToPrune = maxNonceListSize * (prunePercentSize / 100);
                for (int i = 0; i < numberToPrune; i++) {
                    // Remove earliest
                    nonceList.remove(0);
                }
                LOG.info(numberToPrune + " pruned.");
            }

        }
    }
}
