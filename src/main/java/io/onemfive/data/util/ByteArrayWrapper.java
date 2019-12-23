package io.onemfive.data.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public class ByteArrayWrapper implements NamedStreamable {

    private final String name;
    private final byte[] data;
    private final boolean isDirectory;

    public ByteArrayWrapper(String name) {
        this.name = name;
        this.data = null;
        this.isDirectory = true;
    }

    public ByteArrayWrapper(byte[] data) {
        this.name = null;
        this.data = data;
        this.isDirectory = false;
    }

    public ByteArrayWrapper(String name, byte[] data) {
        this.name = name;
        this.data = data;
        this.isDirectory = false;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public InputStream getInputStream() throws IOException {
        if(data != null)
            return new ByteArrayInputStream(data);
        else if(name != null) {
            return new ByteArrayInputStream(name.getBytes());
        }
        else return null;
    }

    public String getName() {
        return name;
    }

}
