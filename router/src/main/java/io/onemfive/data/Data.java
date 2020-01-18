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
package io.onemfive.data;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public abstract class Data {

    protected byte[] data;

    public Data(){}

    public Data(byte[] data) {
        this.data = data;
    }

    public int length() {
        if(data==null) return 0;
        return data.length;
    }

    public String toBase64() {
        return data == null ? null : Base64.getEncoder().encodeToString(data);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            writeBytes(baos);
            return baos.toByteArray();
    }

    public void writeBytes(OutputStream out) throws IOException {
        out.write(data);
    }

    public int hashCode() {
        if (data == null) {
            return 0;
        } else {
            int d = data[0];
            for(int i = 1; i < 4; ++i) {
                d ^= data[i] << i * 8;
            }
            return d;
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof Data && Arrays.equals(data, ((Data)obj).data);
        }
    }
}
