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

import io.onemfive.core.util.data.DataHelper;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * GZIP implementation per
 * <a href="http://www.faqs.org/rfcs/rfc1952.html">RFC 1952</a>, reusing
 * java's standard CRC32 and Inflater and InflaterInputStream implementations.
 * The main difference is that this implementation allows its state to be
 * reset to initial values, and hence reused, while the standard
 * GZIPInputStream reads the GZIP header from the stream on instantiation.
 *
 */
public class ResettableGZIPInputStream extends InflaterInputStream {
    private static final int FOOTER_SIZE = 8; // CRC32 + ISIZE
    /** See below for why this is necessary */
    private final ExtraByteInputStream extraByteInputStream;
    /** keep a typesafe copy of this */
    private final LookaheadInputStream lookaheadStream;
    private final CRC32 crc32;
    private final byte buf1[] = new byte[1];
    private boolean complete;

    /**
     * Build a new GZIP stream without a bound compressed stream.  You need
     * to initialize this with initialize(compressedStream) when you want to
     * decompress a stream.
     */
    public ResettableGZIPInputStream() {
        // compressedStream ->
        //   LookaheadInputStream that removes last 8 bytes ->
        //     ExtraByteInputStream that adds 1 byte ->
        //       InflaterInputStream
        // See below for why this is necessary
        super(new ExtraByteInputStream(new LookaheadInputStream(FOOTER_SIZE)),
                new Inflater(true));
        extraByteInputStream = (ExtraByteInputStream)in;
        lookaheadStream = (LookaheadInputStream) extraByteInputStream.getInputStream();
        crc32 = new CRC32();
    }

    /**
     * Warning - blocking!
     */
    public ResettableGZIPInputStream(InputStream compressedStream) throws IOException {
        this();
        initialize(compressedStream);
    }

    /**
     * Blocking call to initialize this stream with the data from the given
     * compressed stream.
     *
     */
    public void initialize(InputStream compressedStream) throws IOException {
        len = 0;
        inf.reset();
        complete = false;
        crc32.reset();
        buf1[0] = 0x0;
        extraByteInputStream.reset();
        // blocking call to read the footer/lookahead, and use the compressed
        // stream as the source for further lookahead bytes
        lookaheadStream.initialize(compressedStream);
        // now blocking call to read and verify the GZIP header from the
        // lookahead stream
        verifyHeader();
    }

    @Override
    public int read() throws IOException {
        int read = read(buf1, 0, 1);
        if (read == -1)
            return -1;
        return buf1[0] & 0xff;
    }

    @Override
    public int read(byte buf[]) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte buf[], int off, int len) throws IOException {
        if (complete) {
            // shortcircuit so the inflater doesn't try to refill
            // with the footer's data (which would fail, causing ZLIB err)
            return -1;
        }
        int read = super.read(buf, off, len);
        if (read == -1) {
            verifyFooter();
            return -1;
        } else {
            crc32.update(buf, off, read);
            // NO, we can't do use getEOFReached here
            // 1) Just because the lookahead stream has hit EOF doesn't mean
            //    that the inflater has given us all the data yet,
            //    this would cause data loss at the end
            //if (_lookaheadStream.getEOFReached()) {
            if (inf.finished()) {
                verifyFooter();
                inf.reset(); // so it doesn't complain about missing data...
                complete = true;
            }
            return read;
        }
    }

    public long getTotalRead() {
        try {
            return inf.getBytesRead();
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public long getTotalExpanded() {
        try {
            return inf.getBytesWritten();
        } catch (RuntimeException e) {
            // possible NPE in some implementations
            return 0;
        }
    }

    public long getRemaining() {
        try {
            return inf.getRemaining();
        } catch (RuntimeException e) {
            // possible NPE in some implementations
            return 0;
        }
    }

    public boolean getFinished() {
        try {
            return inf.finished();
        } catch (RuntimeException e) {
            // possible NPE in some implementations
            return true;
        }
    }

    public void close() throws IOException {
        len = 0;
        inf.reset();
        complete = false;
        crc32.reset();
        buf1[0] = 0x0;
        extraByteInputStream.close();
    }

    @Override
    public String toString() {
        return "Read: " + getTotalRead() + " expanded: " + getTotalExpanded() + " remaining: " + getRemaining() + " finished: " + getFinished();
    }

    private void verifyFooter() throws IOException {
        byte footer[] = lookaheadStream.getFooter();

        long actualSize = inf.getTotalOut();
        long expectedSize = DataHelper.fromLongLE(footer, 4, 4);
        if (expectedSize != actualSize)
            throw new IOException("gunzip expected " + expectedSize + " bytes, got " + actualSize);

        long actualCRC = crc32.getValue();
        long expectedCRC = DataHelper.fromLongLE(footer, 0, 4);
        if (expectedCRC != actualCRC)
            throw new IOException("gunzip CRC fail expected 0x" + Long.toHexString(expectedCRC) +
                    " bytes, got 0x" + Long.toHexString(actualCRC));
    }

    /**
     * Make sure the header is valid, throwing an IOException if its b0rked.
     */
    private void verifyHeader() throws IOException {
        int c = in.read();
        if (c != 0x1F) throw new IOException("First magic byte was wrong [" + c + "]");
        c = in.read();
        if (c != 0x8B) throw new IOException("Second magic byte was wrong [" + c + "]");
        c = in.read();
        if (c != 0x08) throw new IOException("Compression format is invalid [" + c + "]");

        int flags = in.read();

        // snag (and ignore) the MTIME
        c = in.read();
        if (c == -1) throw new IOException("EOF on MTIME0 [" + c + "]");
        c = in.read();
        if (c == -1) throw new IOException("EOF on MTIME1 [" + c + "]");
        c = in.read();
        if (c == -1) throw new IOException("EOF on MTIME2 [" + c + "]");
        c = in.read();
        if (c == -1) throw new IOException("EOF on MTIME3 [" + c + "]");

        c = in.read();
        if ( (c != 0x00) && (c != 0x02) && (c != 0x04) )
            throw new IOException("Invalid extended flags [" + c + "]");

        c = in.read(); // ignore creator OS

        // handle flags...
        if (0 != (flags & (1<<5))) {
            // extra header, read and ignore
            int _len = 0;
            c = in.read();
            if (c == -1) throw new IOException("EOF reading the extra header");
            _len = c;
            c = in.read();
            if (c == -1) throw new IOException("EOF reading the extra header");
            _len += (c << 8);

            // now skip that data
            for (int i = 0; i < _len; i++) {
                c = in.read();
                if (c == -1) throw new IOException("EOF reading the extra header's body");
            }
        }

        if (0 != (flags & (1 << 4))) {
            // ignore the name
            c = in.read();
            while (c != 0) {
                if (c == -1) throw new IOException("EOF reading the name");
                c = in.read();
            }
        }

        if (0 != (flags & (1 << 3))) {
            // ignore the comment
            c = in.read();
            while (c != 0) {
                if (c == -1) throw new IOException("EOF reading the comment");
                c = in.read();
            }
        }

        if (0 != (flags & (1 << 6))) {
            // ignore the header CRC16 (we still check the body CRC32)
            c = in.read();
            if (c == -1) throw new IOException("EOF reading the CRC16");
            c = in.read();
            if (c == -1) throw new IOException("EOF reading the CRC16");
        }
    }

    private static class ExtraByteInputStream extends FilterInputStream {
        private static final byte DUMMY = 0;
        private boolean _extraSent;

        public ExtraByteInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            if (_extraSent)
                return -1;
            int rv = in.read();
            if (rv >= 0)
                return rv;
            _extraSent = true;
            return DUMMY;
        }

        @Override
        public int read(byte buf[], int off, int len) throws IOException {
            if (len == 0)
                return 0;
            if (_extraSent)
                return -1;
            int rv = in.read(buf, off, len);
            if (rv >= 0)
                return rv;
            _extraSent = true;
            buf[off] = DUMMY;
            return 1;
        }

        @Override
        public void close() throws IOException {
            _extraSent = false;
            in.close();
        }

        /** does NOT call in.reset() */
        @Override
        public void reset() {
            _extraSent = false;
        }

        public InputStream getInputStream() {
            return in;
        }
    }
}
