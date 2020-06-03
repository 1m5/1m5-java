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
package io.onemfive.monetary.btc;

import io.onemfive.monetary.currency.Coin;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class Satoshi implements Coin {

    private String base58Address;
    private long value = 0L;

    @Override
    public boolean limitedSupply() {
        return true;
    }

    /**
     * 2.1 Quadrillion Satoshis (21 Million Bitcoin)
     * @return long max supply
     */
    @Override
    public long maxSupply() {
        return 2100000000000000L;
    }

    public String getBase58Address() {
        return base58Address;
    }

    public void setBase58Address(String base58Address) {
        this.base58Address = base58Address;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public long value() {
        return value;
    }

    @Override
    public String symbol() {
        return "里";
    }

    public Satoshi(long value) {
        this.value = value;
    }
}
