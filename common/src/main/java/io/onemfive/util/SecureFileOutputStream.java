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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Same as FileOutputStream but sets the file mode so it can only
 * be read and written by the owner only (i.e. 600 on linux)
 */
public class SecureFileOutputStream extends FileOutputStream {

    private static final boolean oneDotSix = SystemVersion.isJava6();

    /**
     *  Sets output file to mode 600
     */
    public SecureFileOutputStream(String file) throws FileNotFoundException {
        super(file);
        setPerms(new File(file));
    }

    /**
     *  Sets output file to mode 600 whether append = true or false
     */
    public SecureFileOutputStream(String file, boolean append) throws FileNotFoundException {
        super(file, append);
        //if (!append)
        setPerms(new File(file));
    }

    /**
     *  Sets output file to mode 600
     */
    public SecureFileOutputStream(File file) throws FileNotFoundException {
        super(file);
        setPerms(file);
    }

    /**
     *  Sets output file to mode 600 only if append = false
     *  (otherwise it is presumed to be 600 already)
     */
    public SecureFileOutputStream(File file, boolean append) throws FileNotFoundException {
        super(file, append);
        //if (!append)
        setPerms(file);
    }

    static boolean canSetPerms() {
        return !oneDotSix;
    }

    /**
     *  Tries to set the permissions to 600,
     *  ignores errors
     */
    public static void setPerms(File f) {
        if (!canSetPerms())
            return;
        try {
            f.setReadable(false, false);
            f.setReadable(true, true);
            f.setWritable(false, false);
            f.setWritable(true, true);
        } catch (Throwable t) {
            // NoSuchMethodException or NoSuchMethodError if we somehow got the
            // version detection wrong or the JVM doesn't support it
        }
    }
}
