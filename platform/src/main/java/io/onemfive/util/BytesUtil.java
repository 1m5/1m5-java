package io.onemfive.util;

public class BytesUtil {

    public static int packBigEndian(byte[] b) {
        return (b[0] & 0xFF) << 24
                | (b[1] & 0xFF) << 16
                | (b[2] & 0xFF) <<  8
                | (b[3] & 0xFF);
    }

    public static byte[] unpackBigEndian(int x) {
        return new byte[] {
                (byte)(x >>> 24),
                (byte)(x >>> 16),
                (byte)(x >>>  8),
                (byte)(x)
        };
    }

    public static int packLittleEndian(byte[] b) {
        return (b[0] & 0xFF)
                | (b[1] & 0xFF) <<  8
                | (b[2] & 0xFF) << 16
                | (b[3] & 0xFF) << 24;
    }

    public static byte[] unpackLittleEndian(int x) {
        return new byte[]{
                (byte) (x),
                (byte) (x >>> 8),
                (byte) (x >>> 16),
                (byte) (x >>> 24)
        };
    }
}
