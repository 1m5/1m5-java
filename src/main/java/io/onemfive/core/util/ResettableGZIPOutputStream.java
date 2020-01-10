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
package io.onemfive.core.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * GZIP implementation per
 * <a href="http://www.faqs.org/rfcs/rfc1952.html">RFC 1952</a>, reusing
 * java's standard CRC32 and Deflater implementations.  The main difference
 * is that this implementation allows its state to be reset to initial
 * values, and hence reused, while the standard GZIPOutputStream writes the
 * GZIP header to the stream on instantiation, rather than on first write.
 *
 */
public class ResettableGZIPOutputStream extends DeflaterOutputStream {
    /** has the header been written out yet? */
    private boolean _eaderWritten;
    /** how much data is in the uncompressed stream? */
    private long writtenSize;
    private final CRC32 crc32;
    private static final boolean DEBUG = false;

    public ResettableGZIPOutputStream(OutputStream o) {
        super(o, new Deflater(9, true));
        crc32 = new CRC32();
    }

    /**
     * Reinitialze everything so we can write a brand new gzip output stream
     * again.
     */
    public void reset() {
        if (DEBUG)
            System.out.println("Resetting (writtenSize=" + writtenSize + ")");
        def.reset();
        crc32.reset();
        writtenSize = 0;
        _eaderWritten = false;
    }

    private static final byte[] HEADER = new byte[] {
            (byte)0x1F, (byte)0x8b, // magic bytes
            0x08,                   // compression format == DEFLATE
            0x00,                   // flags (NOT using CRC16, filename, etc)
            0x00, 0x00, 0x00, 0x00, // no modification time available (don't leak this!)
            0x02,                   // maximum compression
            (byte)0xFF              // unknown creator OS (!!!)
    };

    /**
     * obviously not threadsafe, but its a stream, thats standard
     */
    private void ensureHeaderIsWritten() throws IOException {
        if (_eaderWritten) return;
        if (DEBUG) System.out.println("Writing header");
        out.write(HEADER);
        _eaderWritten = true;
    }

    private void writeFooter() throws IOException {
        // damn RFC writing their bytes backwards...
        long crcVal = crc32.getValue();
        out.write((int)(crcVal & 0xFF));
        out.write((int)((crcVal >>> 8) & 0xFF));
        out.write((int)((crcVal >>> 16) & 0xFF));
        out.write((int)((crcVal >>> 24) & 0xFF));

        long sizeVal = writtenSize; // % (1 << 31) // *redundant*
        out.write((int)(sizeVal & 0xFF));
        out.write((int)((sizeVal >>> 8) & 0xFF));
        out.write((int)((sizeVal >>> 16) & 0xFF));
        out.write((int)((sizeVal >>> 24) & 0xFF));
        out.flush();
        if (DEBUG) {
            System.out.println("Footer written: crcVal=" + crcVal + " sizeVal=" + sizeVal + " written=" + writtenSize);
            System.out.println("size hex: " + Long.toHexString(sizeVal));
            System.out.print(  "size2 hex:" + Long.toHexString((int)(sizeVal & 0xFF)));
            System.out.print(  Long.toHexString((int)((sizeVal >>> 8) & 0xFF)));
            System.out.print(  Long.toHexString((int)((sizeVal >>> 16) & 0xFF)));
            System.out.print(  Long.toHexString((int)((sizeVal >>> 24) & 0xFF)));
            System.out.println();
        }
    }

    @Override
    public void close() throws IOException {
        finish();
        super.close();
    }
    @Override
    public void finish() throws IOException {
        ensureHeaderIsWritten();
        super.finish();
        writeFooter();
    }

    @Override
    public void write(int b) throws IOException {
        ensureHeaderIsWritten();
        crc32.update(b);
        writtenSize++;
        super.write(b);
    }
    @Override
    public void write(byte buf[]) throws IOException {
        write(buf, 0, buf.length);
    }
    @Override
    public void write(byte buf[], int off, int len) throws IOException {
        ensureHeaderIsWritten();
        crc32.update(buf, off, len);
        writtenSize += len;
        super.write(buf, off, len);
    }

}
