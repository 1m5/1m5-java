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
package io.onemfive.util;

import java.io.File;
import java.io.IOException;

/**
 * Same as SecureDirectory but sets the file mode after createNewFile()
 * and createTempFile() also. So just use this instead.
 */
public class SecureFile extends File {

    protected static final boolean isNotWindows = !SystemVersion.isWindows();

    public SecureFile(String pathname) {
        super(pathname);
    }

    public SecureFile(String parent, String child) {
        super(parent, child);
    }

    public SecureFile(File parent, String child) {
        super(parent, child);
    }

    /**
     *  Sets file to mode 600 if the file is created
     */
    @Override
    public boolean createNewFile() throws IOException {
        boolean rv = super.createNewFile();
        if (rv)
            setPerms();
        return rv;
    }

    /**
     *  Sets directory to mode 700 if the directory is created
     */
    @Override
    public boolean mkdir() {
        boolean rv = super.mkdir();
        if (rv)
            setPerms();
        return rv;
    }

    /**
     *  Sets directory to mode 700 if the directory is created
     *  Does NOT change the mode of other created directories
     */
    @Override
    public boolean mkdirs() {
        boolean rv = super.mkdirs();
        if (rv)
            setPerms();
        return rv;
    }

    /**
     *  Sets file to mode 600 when the file is created
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        File rv = File.createTempFile(prefix, suffix);
        // same thing as below but static
        SecureFileOutputStream.setPerms(rv);
        return rv;
    }

    /**
     *  Sets file to mode 600 when the file is created
     */
    public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
        File rv = File.createTempFile(prefix, suffix, directory);
        // same thing as below but static
        SecureFileOutputStream.setPerms(rv);
        return rv;
    }

    /**
     *  Tries to set the permissions to 600,
     *  ignores errors
     */
    protected void setPerms() {
        if (!SecureFileOutputStream.canSetPerms())
            return;
        try {
            setReadable(false, false);
            setReadable(true, true);
            setWritable(false, false);
            setWritable(true, true);
            if (isNotWindows && isDirectory()) {
                setExecutable(false, false);
                setExecutable(true, true);
            }
        } catch (Throwable t) {
            // NoSuchMethodException or NoSuchMethodError if we somehow got the
            // version detection wrong or the JVM doesn't support it
        }
    }
}
