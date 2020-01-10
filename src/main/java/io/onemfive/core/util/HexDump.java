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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Hexdump class (well, it's actually a namespace with some functions,
 * but let's stick with java terminology :-).  These methods generate
 * an output that resembles `hexdump -C` (Windows users: do you
 * remember `debug` in the DOS age?).
 */
public class HexDump {

    private static final int FORMAT_OFFSET_PADDING = 8;
    private static final int FORMAT_BYTES_PER_ROW = 16;
    private static final byte[] HEXCHARS = DataHelper.getASCII("0123456789abcdef");

    /**
     * Dump a byte array in a String.
     *
     * @param data Data to be dumped
     */
    public static String dump(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            dump(data, 0, data.length, out);
            return out.toString("ISO-8859-1");
        } catch (IOException e) {
            throw new RuntimeException("no 8859?", e);
        }
    }

    /**
     * Dump a byte array in a String.
     *
     * @param data Data to be dumped
     * @param off  Offset from the beginning of <code>data</code>
     * @param len  Number of bytes of <code>data</code> to be dumped
     */
    public static String dump(byte[] data, int off, int len) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            dump(data, off, len, out);
            return out.toString("ISO-8859-1");
        } catch (IOException e) {
            throw new RuntimeException("no 8859?", e);
        }
    }

    /**
     * Dump a byte array through a stream.
     *
     * @param data Data to be dumped
     * @param out  Output stream
     */
    public static void dump(byte data[], OutputStream out) throws IOException {
        dump(data, 0, data.length, out);
    }

    /**
     * Dump a byte array through a stream.
     *
     * @param data Data to be dumped
     * @param off  Offset from the beginning of <code>data</code>
     * @param len  Number of bytes of <code>data</code> to be dumped
     * @param out  Output stream
     */
    public static void dump(byte[] data, int off, int len, OutputStream out) throws IOException {
        String hexoff;
        int dumpoff, hexofflen, i, nextbytes, end = len + off;
        int val;

        for (dumpoff = off; dumpoff < end; dumpoff += FORMAT_BYTES_PER_ROW) {
            // Pad the offset with 0's (i miss my beloved sprintf()...)
            hexoff = Integer.toString(dumpoff, 16);
            hexofflen = hexoff.length();
            for (i = 0; i < FORMAT_OFFSET_PADDING - hexofflen; ++i) {
                out.write('0');
            }
            out.write(DataHelper.getASCII(hexoff));
            out.write(' ');

            // Bytes to be printed in the current line
            nextbytes = (FORMAT_BYTES_PER_ROW < (end - dumpoff) ? FORMAT_BYTES_PER_ROW : (end - dumpoff));

            for (i = 0; i < FORMAT_BYTES_PER_ROW; ++i) {
                // Put two spaces to separate 8-bytes blocks
                if ((i % 8) == 0) {
                    out.write(' ');
                }
                if (i >= nextbytes) {
                    out.write(DataHelper.getASCII("   "));
                } else {
                    val = data[dumpoff + i] & 0xff;
                    out.write(HEXCHARS[val >>> 4]);
                    out.write(HEXCHARS[val & 0xf]);
                    out.write(' ');
                }
            }

            out.write(DataHelper.getASCII(" |"));

            for (i = 0; i < FORMAT_BYTES_PER_ROW; ++i) {
                if (i >= nextbytes) {
                    out.write(' ');
                } else {
                    val = data[i + dumpoff];
                    // Is it a printable character?
                    if ((val > 31) && (val < 127)) {
                        out.write(val);
                    } else {
                        out.write('.');
                    }
                }
            }

            out.write('|');
            out.write('\n');
        }
    }

}
