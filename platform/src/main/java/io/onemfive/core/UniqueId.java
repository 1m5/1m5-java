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
package io.onemfive.core;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class UniqueId implements Comparable<UniqueId> {

    public static final byte LENGTH = 32;

    private byte[] bytes;

    /**
     * Create a random <code>UniqueId</code>.
     */
    public UniqueId() {
        bytes = new byte[LENGTH];
        for (int i=0; i<LENGTH; i++)
            bytes[i] = (byte)new Random().nextInt(256);
    }

    /**
     * Create a packet id from 32 bytes of an array, starting at <code>offset</code>.
     * @param bytes
     */
    public UniqueId(byte[] bytes, int offset) {
        this.bytes = new byte[LENGTH];
        System.arraycopy(bytes, offset, this.bytes, 0, LENGTH);
    }
    
    /**
     * Creates a <code>UniqueId</code> using data read from a {@link ByteBuffer}.
     * @param buffer
     */
    public UniqueId(ByteBuffer buffer) {
        bytes = new byte[LENGTH];
        buffer.get(bytes);
    }
    
    /**
     * @param base64 A 44-character base64-encoded string
     */
    public UniqueId(String base64) {
        bytes = Base64.getDecoder().decode(base64);
    }
    
    public byte[] toByteArray() {
        return bytes;
    }
    
    public String toBase64() {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(bytes);
    }
    
    @Override
    public int compareTo(UniqueId otherPacketId) {
        return new BigInteger(1, bytes).compareTo(new BigInteger(1, otherPacketId.bytes));
    }
    
    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject == null)
            return false;
        if (!(anotherObject.getClass() == getClass()))
            return false;
        UniqueId otherPacketId = (UniqueId)anotherObject;
        
        return Arrays.equals(bytes, otherPacketId.bytes);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}