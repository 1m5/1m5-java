package io.onemfive.data;

import io.onemfive.data.util.Base64;
import io.onemfive.data.util.DataFormatException;
import io.onemfive.data.util.DataHelper;
import io.onemfive.data.util.HashUtil;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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

    public String toBase64() throws DataFormatException, IOException {
        return data == null ? null : Base64.encode(data);
    }

    public void fromBase64(String data) throws DataFormatException {
        if (data == null) {
            throw new DataFormatException("Null data passed in");
        } else {
            byte[] bytes = Base64.decode(data);
            if (bytes == null) {
                throw new DataFormatException("Bad Base64 \"" + data + '"');
            } else {
                fromByteArray(bytes);
            }
        }
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public Hash calculateHash(Hash.Algorithm algorithm) throws NoSuchAlgorithmException {
        return data != null ? HashUtil.generateHash(data, algorithm) : null;
    }

    public byte[] toByteArray() throws DataFormatException, IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
            writeBytes(baos);
            return baos.toByteArray();
    }

    public void writeBytes(OutputStream out) throws DataFormatException, IOException {
        out.write(data);
    }

    public void fromByteArray(byte[] data) throws DataFormatException {
        if (data == null) {
            throw new DataFormatException("Null data passed in");
        } else {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                readBytes(bais);
            } catch (IOException var3) {
                throw new DataFormatException("Error reading the byte array", var3);
            }
        }
    }

    public void readBytes(InputStream in) throws DataFormatException, IOException {
        int length = this.length();
        data = new byte[length];
        read(in, data);
    }

    protected int read(InputStream in, byte[] target) throws IOException {
        return DataHelper.read(in, target);
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
