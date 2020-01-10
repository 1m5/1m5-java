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
package io.onemfive.core.util.data;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Encodes and decodes to and from Base64 notation.
 */
public class Base64 {

    /**
     *  Output will be a multiple of 4 chars, including 0-2 trailing '='
     *
     *  @param source if null will return ""
     */
    public static String encode(String source) {
        return (source != null ? encode(DataHelper.getUTF8(source)) : "");
    }

    /**
     *  Output will be a multiple of 4 chars, including 0-2 trailing '='
     *  @param source if null will return ""
     */
    public static String encode(byte[] source) {
        return (source != null ? encode(source, 0, source.length) : "");
    }

    /**
     *  Output will be a multiple of 4 chars, including 0-2 trailing '='
     *  @param source if null will return ""
     */
    public static String encode(byte[] source, int off, int len) {
        return (source != null ? encode(source, off, len, false) : "");
    }

    /**
     *  Output will be a multiple of 4 chars, including 0-2 trailing '='
     *  @param source if null will return ""
     *  @param useStandardAlphabet Warning, must be false for I2P compatibility
     */
    public static String encode(byte[] source, boolean useStandardAlphabet) {
        return (source != null ? encode(source, 0, source.length, useStandardAlphabet) : "");
    }

    /**
     *  Output will be a multiple of 4 chars, including 0-2 trailing '='
     *  @param source if null will return ""
     *  @param useStandardAlphabet Warning, must be false for I2P compatibility
     */
    public static String encode(byte[] source, int off, int len, boolean useStandardAlphabet) {
        return (source != null ? safeEncode(source, off, len, useStandardAlphabet) : "");
    }

    /**
     *  Decodes data from Base64 notation using the I2P alphabet.
     *
     *  @param s Base 64 encoded string using the I2P alphabet A-Z, a-z, 0-9, -, ~
     *  @return the decoded data, null on error
     */
    public static byte[] decode(String s) {
        return safeDecode(s, false);
    }

    /**
     *  Decodes data from Base64 notation using the I2P alphabet.
     *
     *  @param useStandardAlphabet Warning, must be false for I2P compatibility
     *  @return the decoded data, null on error
     */
    public static byte[] decode(String s, boolean useStandardAlphabet) {
        return safeDecode(s, useStandardAlphabet);
    }

    /** Maximum line length (76) of Base64 output. */
    private final static int MAX_LINE_LENGTH = 76;

    /** The equals sign (=) as a byte. */
    private final static byte EQUALS_SIGN = (byte) '=';

    /** The 64 valid Base64 values. */
    private final static byte[] ALPHABET = { (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
            (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
            (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R',
            (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X',
            (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
            (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
            (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p',
            (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
            (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1',
            (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) '+', (byte) '/'};

    /**
     *  The I2P Alphabet.
     */
    public static final String ALPHABET_I2P = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-~";

    /** The 64 valid Base64 values for I2P. */
    private final static byte[] ALPHABET_ALT = DataHelper.getASCII(ALPHABET_I2P);

    /**
     * Translates a Base64 value to either its 6-bit reconstruction value
     * or a negative number indicating some other meaning.
     **/
    private final static byte[] DECODABET = { -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal  0 -  8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 - 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            //62, -9, -9, -9, 63, // + , - . / (43-47) NON-I2P
            -9, -9, 62, -9, -9,   // + , - . / (43-47) I2P
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through 'N'
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O' through 'Z'
            -9, -9, -9, -9, -9, -9, // Decimal 91 - 96
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a' through 'm'
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n' through 'z'
            //-9, -9, -9, -9 // Decimal 123 - 126 (126 is '~') NON-I2P
            -9, -9, -9, 63   // Decimal 123 - 126 (126 is '~') I2P
            ,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 127 - 139
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 140 - 152
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 153 - 165
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 166 - 178
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 179 - 191
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 192 - 204
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 205 - 217
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 218 - 230
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 231 - 243
            -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9         // Decimal 244 - 255
    };


    private final static byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding
    private final static byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in encoding

    /** Defeats instantiation. */
    private Base64() { // nop
    }

    public static void main(String[] args) {
        //test();
        if (args.length == 0) {
            help();
        }
        runApp(args);
    }

    private static void runApp(String args[]) {
        String cmd = args[0].toLowerCase(Locale.US);
        if ("encodestring".equals(cmd)) {
            if (args.length != 2)
                help();
            System.out.println(encode(DataHelper.getUTF8(args[1])));
            return;
        }
        if ("decodestring".equals(cmd)) {
            if (args.length != 2)
                help();
            byte[] dec = decode(args[1]);
            if (dec != null) {
                try {
                    System.out.write(dec);
                } catch (IOException ioe) {
                    System.err.println("output error " + ioe);
                    System.exit(1);
                }
            } else {
                System.err.println("decode error");
                System.exit(1);
            }
            return;
        }
        if ("test".equals(cmd)) {
            System.err.println("test disabled");
            System.exit(1);
        }
        if (!("encode".equals(cmd) || "decode".equals(cmd))) {
            System.err.println("unknown command " + cmd);
            System.exit(1);
        }
        InputStream in = System.in;
        OutputStream out = System.out;
        try {
            if (args.length >= 3) {
                out = new FileOutputStream(args[2]);
            }
            if (args.length >= 2) {
                in = new FileInputStream(args[1]);
            }
            if ("encode".equals(cmd)) {
                encode(in, out);
            } else {
                decode(in, out);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        } finally {
            try { in.close(); } catch (IOException e) {}
            try { out.close(); } catch (IOException e) {}
        }
    }

    private static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        DataHelper.copy(in, baos);
        return baos.toByteArray();
    }

    private static void encode(InputStream in, OutputStream out) throws IOException {
        String encoded = encode(read(in));
        for (int i = 0; i < encoded.length(); i++)
            out.write((byte)(encoded.charAt(i) & 0xFF));
    }

    private static void decode(InputStream in, OutputStream out) throws IOException {
        byte decoded[] = decode(DataHelper.getUTF8(read(in)));
        if (decoded == null)
            throw new IOException("Invalid base 64 string");
        out.write(decoded);
    }

    /** exits 1, never returns */
    private static void help() {
        System.err.println("Usage: Base64 encode <inFile> <outFile>");
        System.err.println("       Base64 encode <inFile>");
        System.err.println("       Base64 encode (stdin to stdout)");
        System.err.println("       Base64 decode <inFile> <outFile>");
        System.err.println("       Base64 decode <inFile>");
        System.err.println("       Base64 decode (stdin to stdout)");
        System.err.println("       Base64 encodestring 'string to encode'");
        System.err.println("       Base64 decodestring 'string to decode'");
        System.err.println("       Base64 test");
        System.exit(1);
    }

    /**
     *  @param alpha alphabet
     */
    private static void encode3to4(byte[] source, int srcOffset, int numSigBytes, StringBuilder buf, byte alpha[]) {
        //           1         2         3
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------|    ||    ||    ||    | Six bit groups to index ALPHABET
        //          >>18  >>12  >> 6  >> 0  Right shift necessary
        //                0x3f  0x3f  0x3f  Additional AND

        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an int.
        int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
                | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
                | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

        switch (numSigBytes) {
            case 3:
                buf.append((char)alpha[(inBuff >>> 18)]);
                buf.append((char)alpha[(inBuff >>> 12) & 0x3f]);
                buf.append((char)alpha[(inBuff >>> 6) & 0x3f]);
                buf.append((char)alpha[(inBuff) & 0x3f]);
                return;

            case 2:
                buf.append((char)alpha[(inBuff >>> 18)]);
                buf.append((char)alpha[(inBuff >>> 12) & 0x3f]);
                buf.append((char)alpha[(inBuff >>> 6) & 0x3f]);
                buf.append((char)EQUALS_SIGN);
                return;

            case 1:
                buf.append((char)alpha[(inBuff >>> 18)]);
                buf.append((char)alpha[(inBuff >>> 12) & 0x3f]);
                buf.append((char)EQUALS_SIGN);
                buf.append((char)EQUALS_SIGN);
                return;

            default:
                return;
        } // end switch
    }

    /**
     * Same as encodeBytes, except uses a filesystem / URL friendly set of characters,
     * replacing / with ~, and + with -
     */
    private static String safeEncode(byte[] source, int off, int len, boolean useStandardAlphabet) {
        if (len + off > source.length)
            throw new ArrayIndexOutOfBoundsException("Trying to encode too much!  source.len=" + source.length + " off=" + off + " len=" + len);
        StringBuilder buf = new StringBuilder(len * 4 / 3);
        if (useStandardAlphabet)
            encodeBytes(source, off, len, false, buf, ALPHABET);
        else
            encodeBytes(source, off, len, false, buf, ALPHABET_ALT);
        return buf.toString();
    }

    /**
     * Same as decode, except from a filesystem / URL friendly set of characters,
     * replacing / with ~, and + with -
     */
    private static byte[] safeDecode(String source, boolean useStandardAlphabet) {
        if (source == null) return null;
        String toDecode;
        if (useStandardAlphabet) {
            //toDecode = source;
            toDecode = source.replace('/', '~');
            toDecode = toDecode.replace('+', '-');
        } else {
            //toDecode = source.replace('~', '/');
            //toDecode = toDecode.replace('-', '+');
            toDecode = source;
        }
        return standardDecode(toDecode);
    }

    /**
     * Encodes a byte array into Base64 notation.
     *
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     * @param breakLines Break lines at 80 characters or less.
     */
    private static void encodeBytes(byte[] source, int off, int len, boolean breakLines, StringBuilder out, byte alpha[]) {
        //int len43 = len * 4 / 3;
        //byte[] outBuff = new byte[(len43) // Main 4:3
        //                          + ((len % 3) > 0 ? 4 : 0) // Account for padding
        //                          + (breakLines ? (len43 / MAX_LINE_LENGTH) : 0)]; // New lines
        int d = 0;
        int len2 = len - 2;
        int lineLength = 0;
        for (; d < len2; d += 3) {
            //encode3to4(source, d + off, 3, outBuff, e);
            encode3to4(source, d + off, 3, out, alpha);

            lineLength += 4;
            if (breakLines && lineLength == MAX_LINE_LENGTH) {
                //outBuff[e + 4] = NEW_LINE;
                out.append('\n');
                lineLength = 0;
            } // end if: end of line
        } // en dfor: each piece of array

        if (d < len) {
            //encode3to4(source, d + off, len - d, outBuff, e);
            encode3to4(source, d + off, len - d, out, alpha);
        } // end if: some padding needed

        //out.append(new String(outBuff, 0, e));
        //return new String(outBuff, 0, e);
    } // end encodeBytes

    /**
     * Decodes four bytes from array <var>source</var>
     * and writes the resulting bytes (up to three of them)
     * to <var>destination</var>.
     * The source and destination arrays can be manipulated
     * anywhere along their length by specifying
     * <var>srcOffset</var> and <var>destOffset</var>.
     * This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 4 for
     * the <var>source</var> array or <var>destOffset</var> + 3 for
     * the <var>destination</var> array.
     * This method returns the actual number of bytes that
     * were converted from the Base64 encoding.
     *
     *
     * @param source the array to convert
     * @param srcOffset the index where conversion begins
     * @param destination the array to hold the conversion
     * @param destOffset the index where output will be put
     * @return the number of decoded bytes converted 1-3, or -1 on error, never zero
     */
    private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
        byte decode0 = DECODABET[source[srcOffset++]];
        byte decode1 = DECODABET[source[srcOffset++]];
        if (decode0 < 0 || decode1 < 0)
            return -1;

        // Example: Dk==
        if (source[srcOffset] == EQUALS_SIGN) {
            if (source[srcOffset + 1] != EQUALS_SIGN)
                return -1;
            // verify no extra bits
            if ((decode1 & 0x0f) != 0)
                return -1;
            int outBuff = (decode0 << 18)
                    | (decode1 << 12);
            destination[destOffset] = (byte) (outBuff >> 16);
            return 1;
        }

        // Example: DkL=
        else if (source[srcOffset + 1] == EQUALS_SIGN) {
            byte decode2 = DECODABET[source[srcOffset]];
            if (decode2 < 0)
                return -1;
            // verify no extra bits
            if ((decode2 & 0x03) != 0)
                return -1;
            int outBuff = (decode0 << 18)
                    | (decode1 << 12)
                    | (decode2 << 6);
            destination[destOffset++] = (byte) (outBuff >> 16);
            destination[destOffset] = (byte) (outBuff >> 8);
            return 2;
        }

        // Example: DkLE
        else {
            byte decode2 = DECODABET[source[srcOffset++]];
            byte decode3 = DECODABET[source[srcOffset]];
            if (decode2 < 0 || decode3 < 0)
                return -1;
            int outBuff = (decode0 << 18)
                    | (decode1 << 12)
                    | (decode2 << 6)
                    | decode3;
            destination[destOffset++] = (byte) (outBuff >> 16);
            destination[destOffset++] = (byte) (outBuff >> 8);
            destination[destOffset] = (byte) (outBuff);
            return 3;
        }
    }

    /**
     * Decodes data from Base64 notation.
     *
     * @param s the string to decode
     * @return the decoded data, null on error
     */
    private static byte[] standardDecode(String s) {
        // We use getUTF8() instead of getASCII() so we may verify
        // there's no UTF-8 in there.
        byte[] bytes = DataHelper.getUTF8(s);
        if (bytes.length != s.length())
            return null;
        return decode(bytes, 0, bytes.length);
    } // end decode

    /**
     * Decodes data from Base64 notation and
     * returns it as a string.
     * Equivlaent to calling
     * <code>new String( decode( s ) )</code>
     *
     * @param s the string to decode
     * @return The data as a string, or null on error
     */
    public static String decodeToString(String s) {
        byte[] b = decode(s);
        if (b == null)
            return null;
        return DataHelper.getUTF8(b);
    } // end decodeToString

    /**
     * Decodes Base64 content in byte array format and returns
     * the decoded byte array.
     *
     * @param source The Base64 encoded data
     * @param off    The offset of where to begin decoding
     * @param len    The length of characters to decode
     * @return decoded data, null on error
     */
    private static byte[] decode(byte[] source, int off, int len) {
        int len34 = len * 3 / 4;
        byte[] outBuff = new byte[len34]; // size of output
        int outBuffPosn = 0;

        int i = off;
        int end = off + len;
        int converted = 0;
        while (i + 3 < end) {
            converted = decode4to3(source, i, outBuff, outBuffPosn);
            if (converted < 0)
                return null;
            outBuffPosn += converted;
            i += 4;
            if (converted < 3)
                break;
        }

        // process any remaining without '='
        int remaining = end - i;
        if (remaining > 0) {
            if (converted > 0 && converted < 3)
                return null;
            if (remaining == 1 || remaining > 3)
                return null;
            byte[] b4 = new byte[4];
            b4[0] = source[i++];
            b4[1] = source[i++];
            if (remaining == 3)
                b4[2] = source[i];
            else
                b4[2] = EQUALS_SIGN;
            b4[3] = EQUALS_SIGN;
            converted = decode4to3(b4, 0, outBuff, outBuffPosn);
            if (converted < 0)
                return null;
            outBuffPosn += converted;
        }

        // don't copy unless we have to
        if (outBuffPosn == outBuff.length)
            return outBuff;
        // and we shouldn't ever... would have returned null before
        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }
}
