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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * General helper methods for messing with files
 *
 * These are static methods that do NOT convert arguments
 * to absolute paths for a particular context and directory.
 *
 * Callers should ALWAYS provide absolute paths as arguments,
 * and should NEVER assume files are in the current working directory.
 */
public class FileUtil {

    private static Logger LOG = Logger.getLogger(FileUtil.class.getName());

    private static boolean failedOracle;
    private static boolean failedApache;

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    /**
     * Opens a <code>URL</code> and reads one line at a time.
     * Returns the lines as a <code>List</code> of <code>String</code>s,
     * or an empty <code>List</code> if an error occurred.
     * @param url
     * @see #readLines(File)
     */
    public static List<String> readLines(URL url) {
        LOG.info("Reading URL: <" + url + ">");

        InputStream stream = null;
        try {
            stream = url.openStream();
            return readLines(stream);
        }
        catch (IOException e) {
            LOG.warning("Error reading URL.");
            return Collections.emptyList();
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warning("Can't close input stream.");
                }
        }
    }

    /**
     * Opens a <code>File</code> and reads one line at a time.
     * Returns the lines as a <code>List</code> of <code>String</code>s,
     * or an empty <code>List</code> if an error occurred.
     * @param file
     * @see #readLines(URL)
     */
    public static List<String> readLines(File file) {
        LOG.info("Reading file: <" + file.getAbsolutePath() + ">");

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            return readLines(stream);
        } catch (IOException e) {
            LOG.warning("Error reading file.");
            return Collections.emptyList();
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warning("Can't close input stream.");
                }
        }
    }

    public static List<String> readLines(String fileName) throws IOException {
        File f = new File(fileName);
        LOG.info("Loading lines in file "+f.getAbsolutePath()+"...");
        return Files.readAllLines(f.toPath(), Charset.defaultCharset());
    }

    /**
     * Opens an <code>InputStream</code> and reads one line at a time.
     * Returns the lines as a <code>List</code> of <code>String</code>s.
     * or an empty <code>List</code> if an error occurred.
     * @param inputStream
     * @see #readLines(URL)
     */
    public static List<String> readLines(InputStream inputStream) throws IOException {
        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = new ArrayList<String>();

        while (true) {
            String line = null;
            line = inputBuffer.readLine();
            if (line == null)
                break;
            lines.add(line);
        }

        LOG.info(lines.size() + " lines read.");
        return lines;
    }

    /**
     * Reads all data from an input stream and writes it to an output stream.
     * @param input
     * @param output
     * @throws IOException
     */
    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024*1024];
        int bytesRead;
        while ((bytesRead=input.read(buffer)) >= 0)
            output.write(buffer, 0, bytesRead);
    }

    public static void copy(File from, File to) throws IOException {
        if (!to.exists())
            to.createNewFile();

        FileChannel fromChan = null;
        FileChannel toChan = null;
        try {
            fromChan = new FileInputStream(from).getChannel();
            toChan = new FileOutputStream(to).getChannel();
            toChan.transferFrom(fromChan, 0, fromChan.size());
        }
        finally {
            if (fromChan != null)
                fromChan.close();
            if (toChan != null)
                toChan.close();

            // This is needed on Windows so a file can be deleted after copying it.
            System.gc();
        }
    }

    /**
     * Tests if a directory contains a file with a given name.
     * @param directory
     * @param filename
     */
    public static boolean contains(File directory, final String filename) {
        String[] matches = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(filename);
            }
        });

        return matches!=null && matches.length>0;
    }

    /**
     * Delete the path as well as any files or directories underneath it.
     *
     * @param path path to the directory being deleted
     * @param failIfNotEmpty if true, do not delete anything if the directory
     *                       is not empty (and return false)
     * @return true if the path no longer exists (aka was removed),
     *         false if it remains
     */
    public static final boolean rmdir(String path, boolean failIfNotEmpty) {
        return rmdir(new File(path), failIfNotEmpty);
    }
    /**
     * Delete the path as well as any files or directories underneath it.
     *
     * @param target the file or directory being deleted
     * @param failIfNotEmpty if true, do not delete anything if the directory
     *                       is not empty (and return false)
     * @return true if the path no longer exists (aka was removed),
     *         false if it remains
     */
    public static final boolean rmdir(File target, boolean failIfNotEmpty) {
        if (!target.exists()) {
            //System.out.println("info: target does not exist [" + target.getPath() + "]");
            return true;
        }
        if (!target.isDirectory()) {
            //System.out.println("info: target is not a directory [" + target.getPath() + "]");
            return target.delete();
        } else {
            File children[] = target.listFiles();
            if (children == null) {
                //System.out.println("info: target null children [" + target.getPath() + "]");
                return false;
            }
            if ( (failIfNotEmpty) && (children.length > 0) ) {
                //System.out.println("info: target is not emtpy[" + target.getPath() + "]");
                return false;
            }
            for (int i = 0; i < children.length; i++) {
                if (!rmdir(children[i], failIfNotEmpty))
                    return false;

                //System.out.println("info: target removed recursively [" + children[i].getPath() + "]");
            }
            return target.delete();
        }
    }

    public static boolean rmFile(String path) {
        return new File(path).delete();
    }

    /**
     * Warning - do not call any new classes from here, or
     * update will crash the JVM.
     *
     * @return true if it was copied successfully
     */
    public static boolean extractZip(File zipfile, File targetDir) {
        int files = 0;
        ZipFile zip = null;
        try {
            final byte buf[] = new byte[8192];
            zip = new ZipFile(zipfile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                if (entry.getName().contains("src/main")) {
                    LOG.warning("Refusing to extract a zip entry with '..' in it [" + entry.getName() + "]");
                    return false;
                }
                if (entry.getName().indexOf(0) >= 0) {
                    LOG.warning("Refusing to extract a zip entry with null in it [" + entry.getName() + "]");
                    return false;
                }
                File target = new File(targetDir, entry.getName());
                File parent = target.getParentFile();
                if ( (parent != null) && (!parent.exists()) ) {
                    boolean parentsOk = parent.mkdirs();
                    if (!parentsOk) {
                        LOG.warning("Unable to create the parent dir for " + entry.getName() + ": [" + parent.getAbsolutePath() + "]");
                        return false;
                    }
                }
                if (entry.isDirectory()) {
                    if (!target.exists()) {
                        boolean created = target.mkdirs();
                        if (!created) {
                            LOG.warning("Unable to create the directory [" + entry.getName() + "]");
                            return false;
                        } else {
                            LOG.info("Creating directory [" + entry.getName() + "]");
                        }
                    }
                } else {
                    InputStream in = null;
                    FileOutputStream fos = null;
                    JarOutputStream jos = null;
                    try {
                        in = zip.getInputStream(entry);
                        if (entry.getName().endsWith(".jar.pack") || entry.getName().endsWith(".war.pack")) {
                            target = new File(targetDir, entry.getName().substring(0, entry.getName().length() - ".pack".length()));
                            jos = new JarOutputStream(new FileOutputStream(target));
                            unpack(in, jos);
                            LOG.info("File [" + entry.getName() + "] extracted and unpacked");
                        } else {
                            fos = new FileOutputStream(target);
                            // We do NOT use DataHelper.copy() because it loads new classes
                            // and causes the update to crash.
                            //DataHelper.copy(in, fos);
                            int read;
                            while ((read = in.read(buf)) != -1) {
                                fos.write(buf, 0, read);
                            }
                            LOG.info("File [" + entry.getName() + "] extracted");
                        }
                        files++;
                    } catch (IOException ioe) {
                            LOG.warning("Error extracting the zip entry (" + entry.getName() + ')');
                            if (ioe.getMessage() != null && ioe.getMessage().indexOf("CAFED00D") >= 0)
                                LOG.warning("This may be caused by a packed library that requires Java 1.6, your Java version is: " +
                                        System.getProperty("java.version"));
                            ioe.printStackTrace();
                        return false;
                    } catch (Exception e) {
                        // Oracle unpack() should throw an IOE but other problems can happen, e.g:
                        // java.lang.reflect.InvocationTargetException
                        // Caused by: java.util.zip.ZipException: duplicate entry: xxxxx
                        LOG.warning("Error extracting the zip entry (" + entry.getName() + "): "+e.getLocalizedMessage());
                        return false;
                    } finally {
                        try { if (in != null) in.close(); } catch (IOException ioe) {}
                        try { if (fos != null) fos.close(); } catch (IOException ioe) {}
                        try { if (jos != null) jos.close(); } catch (IOException ioe) {}
                    }
                }
            }
            return true;
        } catch (IOException ioe) {
            LOG.warning("Unable to extract the zip file: "+ioe.getLocalizedMessage());
            return false;
        } finally {
            if (zip != null) {
                try { zip.close(); } catch (IOException ioe) {}
            }
            if (files > 0)
                LOG.info(files + " files extracted to " + targetDir);
        }
    }

    /**
     * Verify the integrity of a zipfile.
     * There doesn't seem to be any library function to do this,
     * so we basically go through all the motions of extractZip() above,
     * unzipping everything but throwing away the data.
     *
     * ToDo: verify zip header? Although this would break the undocumented
     *
     * @return true if ok
     */
    public static boolean verifyZip(File zipfile) {
        ZipFile zip = null;
        try {
            byte buf[] = new byte[16*1024];
            zip = new ZipFile(zipfile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            boolean p200TestRequired = true;
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)entries.nextElement();
                if (entry.getName().indexOf("src/main") != -1) {
                    LOG.warning("Refusing to extract a zip entry with '..' in it [" + entry.getName() + "]");
                    return false;
                }
                if (entry.isDirectory()) {
                    // noop
                } else {
                    if (p200TestRequired &&
                            (entry.getName().endsWith(".jar.pack") || entry.getName().endsWith(".war.pack"))) {
                        if (!isPack200Supported()) {
                            LOG.warning("Zip verify failed, your JVM does not support unpack200");
                            return false;
                        }
                        p200TestRequired = false;
                    }
                    try {
                        InputStream in = zip.getInputStream(entry);
                        while ( (in.read(buf)) != -1) {
                            // throw the data away
                        }
                        in.close();
                    } catch (IOException ioe) {
                        LOG.warning(ioe.getLocalizedMessage());
                        return false;
                    }
                }
            }
            return true;
        } catch (IOException ioe) {
           LOG.warning(ioe.getLocalizedMessage());
            return false;
        } finally {
            if (zip != null) {
                try { zip.close(); } catch (IOException ioe) {}
            }
        }
    }

    public static boolean isPack200Supported() {
        try {
            Class.forName("java.util.jar.Pack200", false, ClassLoader.getSystemClassLoader());
            return true;
        } catch (Exception e) {}
        try {
            Class.forName("org.apache.harmony.unpack200.Archive", false, ClassLoader.getSystemClassLoader());
            return true;
        } catch (Exception e) {}
        return false;
    }

    /**
     * Unpack using either Oracle or Apache's unpack200 library,
     * with the classes discovered at runtime so neither is required at compile time.
     *
     * Caller must close streams
     * @throws IOException on unpack error or if neither library is available.
     *         Will not throw ClassNotFoundException.
     * @throws java.lang.reflect.InvocationTargetException on duplicate zip entries in the packed jar
     */
    private static void unpack(InputStream in, JarOutputStream out) throws Exception {
        // For Sun, OpenJDK, IcedTea, etc, use this
        //Pack200.newUnpacker().unpack(in, out);
        if (!failedOracle) {
            try {
                Class<?> p200 = Class.forName("java.util.jar.Pack200", true, ClassLoader.getSystemClassLoader());
                Method newUnpacker = p200.getMethod("newUnpacker");
                Object unpacker = newUnpacker.invoke(null,(Object[])  null);
                Method unpack = unpacker.getClass().getMethod("unpack", InputStream.class, JarOutputStream.class);
                // throws IOException
                unpack.invoke(unpacker, new Object[] {in, out});
                return;
            } catch (ClassNotFoundException e) {
                failedOracle = true;
                LOG.warning(e.getLocalizedMessage());
            } catch (NoSuchMethodException e) {
                failedOracle = true;
                LOG.warning(e.getLocalizedMessage());
            }
        }

        // ------------------
        // For Apache Harmony or if you put its pack200.jar in your library directory use this
        //(new Archive(in, out)).unpack();
        if (!failedApache) {
            try {
                Class<?> p200 = Class.forName("org.apache.harmony.unpack200.Archive", true, ClassLoader.getSystemClassLoader());
                Constructor<?> newUnpacker = p200.getConstructor(InputStream.class, JarOutputStream.class);
                Object unpacker = newUnpacker.newInstance(in, out);
                Method unpack = unpacker.getClass().getMethod("unpack");
                // throws IOException or Pack200Exception
                unpack.invoke(unpacker, (Object[]) null);
                return;
            } catch (ClassNotFoundException e) {
                failedApache = true;
                LOG.warning(e.getLocalizedMessage());
            } catch (NoSuchMethodException e) {
                failedApache = true;
                LOG.warning(e.getLocalizedMessage());
            }
        }

        // ------------------
        // For gcj, gij, etc., use this
        throw new IOException("Unpack200 not supported");
    }

    public static String readTextFileOnClasspath(ClassLoader classLoader, String path, int maxNumLines, boolean startAtBeginning) {
        LOG.info("Loading text file "+path+"...");
        InputStream is = null;
        BufferedReader in;
        try {
            is = classLoader.getResourceAsStream(path);
            if(is==null) {
                LOG.warning("Unable to load file by classloader.");
                return null;
            }
            in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            List<String> lines = new ArrayList<String>(maxNumLines > 0 ? maxNumLines : 64);
            String line = null;
            while ( (line = in.readLine()) != null) {
                lines.add(line);
                if ( (maxNumLines > 0) && (lines.size() >= maxNumLines) ) {
                    if (startAtBeginning)
                        break;
                    else
                        lines.remove(0);
                }
            }
            StringBuilder buf = new StringBuilder(lines.size() * 80);
            for (int i = 0; i < lines.size(); i++) {
                buf.append(lines.get(i)).append('\n');
            }
        } catch (Exception e) {
            LOG.warning("Failed to load text file "+path);
            return null;
        } finally {
            if(is!=null)
                try { is.close();} catch (IOException e) {}
        }
        return in.toString();
    }

    /**
     * Read in the last few lines of a (newline delimited) textfile, or null if
     * the file doesn't exist.
     *
     * Warning - this inefficiently allocates a StringBuilder of size maxNumLines*80,
     *           so don't make it too big.
     * Warning - converts \r\n to \n
     *
     * @param startAtBeginning if true, read the first maxNumLines, otherwise read
     *                         the last maxNumLines
     * @param maxNumLines max number of lines (or -1 for unlimited)
     * @return string or null; does not throw IOException.
     *
     */
    public static String readTextFile(String filename, int maxNumLines, boolean startAtBeginning) {
        File f = new File(filename);
        if (!f.exists()) {
            LOG.warning("No file exists at: "+filename);
        }
        FileInputStream fis = null;
        BufferedReader in = null;
        try {
            fis = new FileInputStream(f);
            in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            List<String> lines = new ArrayList<String>(maxNumLines > 0 ? maxNumLines : 64);
            String line = null;
            while ( (line = in.readLine()) != null) {
                lines.add(line);
                if ( (maxNumLines > 0) && (lines.size() >= maxNumLines) ) {
                    if (startAtBeginning)
                        break;
                    else
                        lines.remove(0);
                }
            }
            StringBuilder buf = new StringBuilder(lines.size() * 80);
            for (int i = 0; i < lines.size(); i++) {
                buf.append(lines.get(i)).append('\n');
            }
            return buf.toString();
        } catch (IOException ioe) {
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (IOException ioe) {}
        }
    }

    /**
     * Load the contents of the given path to a byte array.
     */
    public static byte[] readFile(String path) throws IOException {
        Path fileLocation = Paths.get(path);
        return Files.readAllBytes(fileLocation);
    }

    /**
     * @return true if it was copied successfully
     */
    public static boolean copy(String source, String dest, boolean overwriteExisting) {
        return copy(source, dest, overwriteExisting, false);
    }

    /**
     * @param quiet don't log fails to wrapper log if true
     * @return true if it was copied successfully
     */
    public static boolean copy(String source, String dest, boolean overwriteExisting, boolean quiet) {
        File src = new File(source);
        File dst = new File(dest);
        return copy(src, dst, overwriteExisting, quiet);
    }

    /**
     * @param quiet don't log fails to wrapper log if true
     * @return true if it was copied successfully
     */
    public static boolean copy(File src, File dst, boolean overwriteExisting, boolean quiet) {
        if (dst.exists() && dst.isDirectory())
            dst = new File(dst, src.getName());

        if (!src.exists()) return false;
        if (dst.exists() && !overwriteExisting) return false;

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            // We do NOT use DataHelper.copy() because it's used in installer.jar
            // which does not contain DataHelper
            //DataHelper.copy(in, out);
            int read;
            byte buf[] = new byte[4096];
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            return true;
        } catch (IOException ioe) {
            if (!quiet)
                ioe.printStackTrace();
            return false;
        } finally {
            try { if (in != null) in.close(); } catch (IOException ioe) {}
            try { if (out != null) out.close(); } catch (IOException ioe) {}
        }
    }

    /**
     * Try to rename, if it doesn't work then copy and delete the old.
     * Always overwrites any existing "to" file.
     * Method moved from SingleFileNamingService.
     *
     * @return true if it was renamed / copied successfully
     */
    public static boolean rename(File from, File to) {
        if (!from.exists())
            return false;
        boolean success = false;
        boolean isWindows = SystemVersion.isWindows();
        // overwrite fails on windows
        boolean exists = to.exists();
        if (!isWindows || !exists)
            success = from.renameTo(to);
        if (!success) {
            if (exists && to.delete())
                success = from.renameTo(to);
            if (!success) {
                // hard way
                success = copy(from, to, true, true);
                if (success)
                    from.delete();
            }
        }
        return success;
    }

    /**
     * Write bytes to file
     */
    public static boolean writeFile(byte[] data, String path) {
        try (FileOutputStream stream = new FileOutputStream(path)) {
            stream.write(data);
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

//    public static void main(String args[]) {
//        writeFile("Hello World!".getBytes(),"hw.txt");
//    }

//    /**
//     * Usage: FileUtil (delete path | copy source dest | rename from to | unzip path.zip)
//     *
//     */
//    public static void main(String args[]) {
//        if ( (args == null) || (args.length < 2) ) {
//            System.err.println("Usage: delete path | copy source dest | rename from to | unzip path.zip");
//            //testRmdir();
//        } else if ("delete".equals(args[0])) {
//            boolean deleted = FileUtil.rmdir(args[1], false);
//            if (!deleted)
//                System.err.println("Error deleting [" + args[1] + "]");
//        } else if ("copy".equals(args[0])) {
//            boolean copied = FileUtil.copy(args[1], args[2], false);
//            if (!copied)
//                System.err.println("Error copying [" + args[1] + "] to [" + args[2] + "]");
//        } else if ("unzip".equals(args[0])) {
//            File f = new File(args[1]);
//            File to = new File("tmp");
//            to.mkdir();
//            boolean copied = verifyZip(f);
//            if (!copied)
//                System.err.println("Error verifying " + args[1]);
//            copied = extractZip(f,  to);
//            if (copied)
//                System.err.println("Unzipped [" + args[1] + "] to [" + to + "]");
//            else
//                System.err.println("Error unzipping [" + args[1] + "] to [" + to + "]");
//        } else if ("rename".equals(args[0])) {
//            boolean success = rename(new File(args[1]), new File(args[2]));
//            if (!success)
//                System.err.println("Error renaming [" + args[1] + "] to [" + args[2] + "]");
//        } else {
//            System.err.println("Usage: delete path | copy source dest | rename from to | unzip path.zip");
//        }
//    }

}
