package io.onemfive.core.util.data;
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
import java.io.Serializable;

/**
 * Wrap up an array of bytes so that they can be compared and placed in hashes,
 * maps, and the like.
 */
public class ByteArray implements Serializable, Comparable<ByteArray> {
    private byte[] data;
    private int valid;
    private int offset;

    public ByteArray() {
    }

    /**
     *  Sets valid = data.length, unless data is null
     *  Sets offset = 0
     *  @param data may be null
     */
    public ByteArray(byte[] data) {
        this.data = data;
        valid = (data != null ? data.length : 0);
    }

    /**
     *  Sets offset = offset
     *  Sets valid = length
     *  @param data may be null but why would you do that
     */
    public ByteArray(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        valid = length;
    }

    public byte[] getData() {
        return data;
    }

    /** Warning, does not set valid */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Count how many of the bytes in the array are 'valid'.
     * this property does not necessarily have meaning for all byte
     * arrays.
     */
    public int getValid() { return valid; }
    public void setValid(int valid) { this.valid = valid; }
    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o instanceof ByteArray) {
            ByteArray ba = (ByteArray)o;
            return compare(getData(), offset, valid, ba.getData(), ba.getOffset(), ba.getValid());
        }

        try {
            byte val[] = (byte[]) o;
            return compare(getData(), offset, valid, val, 0, val.length);
        } catch (Throwable t) {
            return false;
        }
    }

    private static final boolean compare(byte[] lhs, int loff, int llen, byte[] rhs, int roff, int rlen) {
        return (llen == rlen) && DataHelper.eq(lhs, loff, rhs, roff, llen);
    }

    public final int compareTo(ByteArray ba) {
        return DataHelper.compareTo(data, ba.getData());
    }

    @Override
    public final int hashCode() {
        return DataHelper.hashCode(getData());
    }

    @Override
    public String toString() {
        return super.toString() + "/" + DataHelper.toString(getData(), 32) + "." + valid;
    }

    public final String toBase64() {
        return Base64.encode(data, offset, valid);
    }
}