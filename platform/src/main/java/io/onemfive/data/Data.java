package io.onemfive.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 * TODO: Add Description
 *
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
