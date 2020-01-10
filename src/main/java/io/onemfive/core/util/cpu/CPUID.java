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
package io.onemfive.core.util.cpu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import io.onemfive.core.OneMFiveAppContext;
import io.onemfive.core.util.data.DataHelper;
import io.onemfive.core.util.FileUtil;
import io.onemfive.core.util.SystemVersion;


/**
 * A class for retrieving details about the CPU using the CPUID assembly instruction.
 */
public class CPUID {

    /** did we load the native lib correctly? */
    private static boolean _nativeOk = false;
    private static int _jcpuidVersion;

    /**
     * do we want to dump some basic success/failure info to stderr during
     * initialization?  this would otherwise use the Log component, but this makes
     * it easier for other systems to reuse this class
     *
     * Well, we really want to use Log so if you are one of those "other systems"
     * then comment out the I2PAppContext usage below.
     *
     * Set to false if not in router context, so scripts using TrustedUpdate
     * don't spew log messages. main() below overrides to true.
     */
    private static boolean _doLog = System.getProperty("jcpuid.dontLog") == null &&
            OneMFiveAppContext.getInstance().isConsciousContext();

    private static final boolean isX86 = SystemVersion.isX86();
    private static final boolean isWindows = SystemVersion.isWindows();
    private static final boolean isLinux = System.getProperty("os.name").toLowerCase(Locale.US).contains("linux");
    private static final boolean isKFreebsd = System.getProperty("os.name").toLowerCase(Locale.US).contains("kfreebsd");
    private static final boolean isFreebsd = (!isKFreebsd) && System.getProperty("os.name").toLowerCase(Locale.US).contains("freebsd");
    private static final boolean isNetbsd = System.getProperty("os.name").toLowerCase(Locale.US).contains("netbsd");
    private static final boolean isOpenbsd = System.getProperty("os.name").toLowerCase(Locale.US).contains("openbsd");
    private static final boolean isSunos = System.getProperty("os.name").toLowerCase(Locale.US).contains("sunos");
    private static final boolean isMac = SystemVersion.isMac();


    /**
     * This isn't always correct.
     * http://stackoverflow.com/questions/807263/how-do-i-detect-which-kind-of-jre-is-installed-32bit-vs-64bit
     * http://mark.koli.ch/2009/10/javas-osarch-system-property-is-the-bitness-of-the-jre-not-the-operating-system.html
     * http://mark.koli.ch/2009/10/reliably-checking-os-bitness-32-or-64-bit-on-windows-with-a-tiny-c-app.html
     * sun.arch.data.model not on all JVMs
     * sun.arch.data.model == 64 => 64 bit processor
     * sun.arch.data.model == 32 => A 32 bit JVM but could be either 32 or 64 bit processor or libs
     * os.arch contains "64" could be 32 or 64 bit libs
     */
    private static final boolean is64 = SystemVersion.is64Bit();

    static
    {
        loadNative();
    }

    /**
     *  A class that can (amongst other things I assume) represent the state of the
     *  different CPU registers after a call to the CPUID assembly method
     */
    protected static class CPUIDResult {
        final int EAX;
        final int EBX;
        final int ECX;
        final int EDX;
        CPUIDResult(int EAX,int EBX,int ECX, int EDX)
        {
            this.EAX = EAX;
            this.EBX = EBX;
            this.ECX = ECX;
            this.EDX = EDX;
        }
    }

    /**
     * Calls the indicated CPUID function and returns the result of the execution
     *
     * @param iFunction The CPUID function to call, should be 0 or larger
     * @return The contents of the CPU registers after the call to the CPUID function
     */
    private static native CPUIDResult doCPUID(int iFunction);

    /**
     *  Get the jbigi version, only available since jbigi version 3
     *  Caller must catch Throwable
     */
    private native static int nativeJcpuidVersion();

    /**
     *  Get the jcpuid version
     *  @return 0 if no jcpuid available, 2 if version not supported
     */
    private static int fetchJcpuidVersion() {
        if (!_nativeOk)
            return 0;
        try {
            return nativeJcpuidVersion();
        } catch (Throwable t) {
            return 2;
        }
    }

    static String getCPUVendorID()
    {
        CPUIDResult c = doCPUID(0);
        StringBuilder sb= new StringBuilder(13);
        sb.append((char)( c.EBX        & 0xFF));
        sb.append((char)((c.EBX >> 8)  & 0xFF));
        sb.append((char)((c.EBX >> 16) & 0xFF));
        sb.append((char)((c.EBX >> 24) & 0xFF));

        sb.append((char)( c.EDX        & 0xFF));
        sb.append((char)((c.EDX >> 8)  & 0xFF));
        sb.append((char)((c.EDX >> 16) & 0xFF));
        sb.append((char)((c.EDX >> 24) & 0xFF));

        sb.append((char)( c.ECX        & 0xFF));
        sb.append((char)((c.ECX >> 8)  & 0xFF));
        sb.append((char)((c.ECX >> 16) & 0xFF));
        sb.append((char)((c.ECX >> 24) & 0xFF));

        return sb.toString();
    }

    static int getCPUFamily()
    {
        CPUIDResult c = doCPUID(1);
        return (c.EAX >> 8) & 0xf;
    }

    static int getCPUModel()
    {
        CPUIDResult c = doCPUID(1);
        return (c.EAX >> 4) & 0xf;
    }

    static int getCPUExtendedModel()
    {
        CPUIDResult c = doCPUID(1);
        return (c.EAX >> 16) & 0xf;
    }

    static int getCPUExtendedFamily()
    {
        CPUIDResult c = doCPUID(1);
        return (c.EAX >> 20) & 0xff;
    }

    static int getEDXCPUFlags()
    {
        CPUIDResult c = doCPUID(1);
        return c.EDX;
    }

    static int getECXCPUFlags()
    {
        CPUIDResult c = doCPUID(1);
        return c.ECX;
    }

    static int getExtendedECXCPUFlags()
    {
        CPUIDResult c = doCPUID(0x80000001);
        return c.ECX;
    }

    static int getExtendedEDXCPUFlags()
    {
        CPUIDResult c = doCPUID(0x80000001);
        return c.EDX;
    }

    static int getExtendedEBXFeatureFlags()
    {
        // Supposed to set ECX to 0 before calling?
        // But we don't have support for that in jcpuid.
        // And it works just fine without that.
        CPUIDResult c = doCPUID(7);
        return c.EBX;
    }

    /**
     * Returns a CPUInfo item for the current type of CPU
     * If I could I would declare this method in a interface named
     * CPUInfoProvider and implement that interface in this class.
     * This would make it easier for other people to understand that there
     * is nothing preventing them from coding up new providers, probably using
     * other detection methods than the x86-only CPUID instruction
     */
    public static CPUInfo getInfo() throws UnknownCPUException
    {
        if(!_nativeOk) {
            throw new UnknownCPUException("Failed to read CPU information from the system. Please verify the existence of the " +
                    getLibraryPrefix() + "jcpuid " + getLibrarySuffix() + " file.");
        }
        String id = getCPUVendorID();
        if(id.equals("CentaurHauls"))
            return new VIAInfoImpl();
        if(!isX86)
            throw new UnknownCPUException("Failed to read CPU information from the system. The CPUID instruction exists on x86 CPUs only.");
        if(id.equals("AuthenticAMD"))
            return new AMDInfoImpl();
        if(id.equals("GenuineIntel"))
            return new IntelInfoImpl();
        throw new UnknownCPUException("Unknown CPU type: '" + id + '\'');
    }

    /**
     * <p>Do whatever we can to load up the native library.
     * If it can find a custom built jcpuid.dll / libjcpuid.so, it'll use that.  Otherwise
     * it'll try to look in the classpath for the correct library (see loadFromResource).
     * If the user specifies -Djcpuid.enable=false it'll skip all of this.</p>
     *
     */
    private static void loadNative() {
        try{
            String wantedProp = System.getProperty("jcpuid.enable", "true");
            boolean wantNative = Boolean.parseBoolean(wantedProp);
            if (wantNative) {
                boolean loaded = loadGeneric();
                if (loaded) {
                    _nativeOk = true;
                    if (_doLog)
                        System.err.println("INFO: Native CPUID library " + getLibraryMiddlePart() + " loaded from file");
                } else {
                    loaded = loadFromResource();
                    if (loaded) {
                        _nativeOk = true;
                        if (_doLog)
                            System.err.println("INFO: Native CPUID library " + getResourceName() + " loaded from resource");
                    } else {
                        _nativeOk = false;
                        if (_doLog)
                            System.err.println("WARNING: Native CPUID library jcpuid not loaded - will not be able to read CPU information using CPUID");
                    }
                }
                _jcpuidVersion = fetchJcpuidVersion();
            } else {
                if (_doLog)
                    System.err.println("INFO: Native CPUID library jcpuid not loaded - will not be able to read CPU information using CPUID");
            }
        }catch(Exception e){
            if (_doLog)
                System.err.println("INFO: Native CPUID library jcpuid not loaded, reason: '"+e.getMessage()+"' - will not be able to read CPU information using CPUID");
        }
    }

    /**
     * <p>Try loading it from an explictly built jcpuid.dll / libjcpuid.so</p>
     * The file name must be (e.g. on linux) either libjcpuid.so or libjcpuid-x86-linux.so.
     * This method does not search for a filename with "_64" in it.
     *
     * @return true if it was loaded successfully, else false
     *
     */
    private static boolean loadGeneric() {
        try {
            System.loadLibrary("jcpuid");
            return true;
        } catch (UnsatisfiedLinkError ule) {
            // fallthrough, try the OS-specific filename
        }

        // Don't bother trying a 64 bit filename variant.

        // 32 bit variant:
        // Note this is unlikely to succeed on a standard installation, since when we extract the library
        // in loadResource() below, we save it as jcpuid.dll / libcupid.so.
        // However, a distribution may put the file in, e.g., /usr/lib/jni/
        // with the libjcpuid-x86-linux.so name.
        // Ubuntu packages now use libjcpuid.so
        //try {
        //    System.loadLibrary(getLibraryMiddlePart());
        //    return true;
        //} catch (UnsatisfiedLinkError ule) {
        return false;
        //}
    }

    /**
     * <p>Check all of the jars in the classpath for the jcpuid dll/so.
     * This file should be stored in the resource in the same package as this class.
     *
     * <p>This is a pretty ugly hack, using the general technique illustrated by the
     * onion FEC libraries.  It works by pulling the resource, writing out the
     * byte stream to a temporary file, loading the native library from that file.
     * We then attempt to copy the file from the temporary dir to the base install dir,
     * so we don't have to do this next time - but we don't complain if it fails,
     * so we transparently support read-only base dirs.
     * </p>
     *
     * This tries the 64 bit version first if we think we may be 64 bit.
     * Then it tries the 32 bit version.
     *
     * @return true if it was loaded successfully, else false
     *
     */
    private static boolean loadFromResource() {
        // Mac info:
        // Through 0.9.25, we had a libjcpuid-x86_64-osx.jnilib and a libjcpuid-x86-osx.jnilib file.
        // As of 0.9.26, we have a single libjcpuid-x86_64-osx.jnilib fat binary that has both 64- and 32-bit support.
        // For updates, the 0.9.27 update contained the new jbigi.jar.
        // However, in rare cases, a user may have skipped that update, going straight
        // from 0.9.26 to 0.9.28. Since we can't be sure, always try both for Mac.
        // getResourceName64() returns non-null for 64-bit OR for 32-bit Mac.

        // try 64 bit first, if getResourceName64() returns non-null
        String resourceName = getResourceName64();
        if (resourceName != null) {
            boolean success = extractLoadAndCopy(resourceName);
            if (success)
                return true;
            if (_doLog)
                System.err.println("WARNING: Resource name [" + resourceName + "] was not found");
        }

        // now try 32 bit
        resourceName = getResourceName();
        boolean success = extractLoadAndCopy(resourceName);
        if (success)
            return true;
        if (_doLog)
            System.err.println("WARNING: Resource name [" + resourceName + "] was not found");
        return false;
    }

    /**
     * Extract a single resource, copy it to a temp location in the file system,
     * and attempt to load it. If the load succeeds, copy it to the installation
     * directory. Return value reflects only load success - copy will fail silently.
     *
     * @return true if it was loaded successfully, else false.
     * @since 0.8.7
     */
    private static boolean extractLoadAndCopy(String resourceName) {
        URL resource = CPUID.class.getClassLoader().getResource(resourceName);
        if (resource == null)
            return false;
        File outFile = null;
        FileOutputStream fos = null;
        String filename = getLibraryPrefix() + "jcpuid" + getLibrarySuffix();
        try {
            InputStream libStream = resource.openStream();
            outFile = new File(OneMFiveAppContext.getInstance().getTempDir(), filename);
            fos = new FileOutputStream(outFile);
            DataHelper.copy(libStream, fos);
            fos.close();
            fos = null;
            System.load(outFile.getAbsolutePath());//System.load requires an absolute path to the lib
        } catch (UnsatisfiedLinkError ule) {
            if (_doLog) {
                System.err.println("WARNING: The resource " + resourceName
                        + " was not a valid library for this platform " + ule);
                //ule.printStackTrace();
            }
            if (outFile != null)
                outFile.delete();
            return false;
        } catch (IOException ioe) {
            if (_doLog) {
                System.err.println("ERROR: Problem writing out the temporary native library data");
                ioe.printStackTrace();
            }
            if (outFile != null)
                outFile.delete();
            return false;
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (IOException ioe) {}
            }
        }
        // copy to install dir, ignore failure
        File newFile = new File(OneMFiveAppContext.getInstance().getBaseDir(), filename);
        FileUtil.copy(outFile, newFile, false, true);
        return true;
    }

    /** @return non-null */
    private static String getResourceName()
    {
        return getLibraryPrefix() + getLibraryMiddlePart() + getLibrarySuffix();
    }

    /**
     * @return null if not on a 64 bit platform (except Mac)
     * @since 0.8.7
     */
    private static String getResourceName64() {
        // As of GMP 6,
        // libjcpuid-x86_64-osx.jnilib file is a fat binary that contains both 64- and 32-bit binaries
        // See loadFromResource() for more info.
        if (!is64 && !isMac)
            return null;
        return getLibraryPrefix() + get64LibraryMiddlePart() + getLibrarySuffix();
    }

    private static String getLibraryPrefix()
    {
        if(isWindows)
            return "";
        else
            return "lib";
    }

    private static String getLibraryMiddlePart(){
        if(isWindows)
            return "jcpuid-x86-windows"; // The convention on Windows
        if(isMac) {
            if(isX86) {
                // As of GMP6,
                // our libjcpuid-x86_64.osx.jnilib is a fat binary,
                // with the 32-bit lib in it also.
                // Not sure if that was on purpose...
                return "jcpuid-x86_64-osx";  // The convention on Intel Macs
            }
            // this will fail, we don't have any ppc libs, but we can't return null here.
            return "jcpuid-ppc-osx";
        }
        if(isKFreebsd)
            return "jcpuid-x86-kfreebsd"; // The convention on kfreebsd...
        if(isFreebsd)
            return "jcpuid-x86-freebsd"; // The convention on freebsd...
        if(isNetbsd)
            return "jcpuid-x86-netbsd"; // The convention on netbsd...
        if(isOpenbsd)
            return "jcpuid-x86-openbsd"; // The convention on openbsd...
        if(isSunos)
            return "jcpuid-x86-solaris"; // The convention on SunOS
        //throw new RuntimeException("Dont know jcpuid library name for os type '"+System.getProperty("os.name")+"'");
        // use linux as the default, don't throw exception
        return "jcpuid-x86-linux";
    }

    /** @since 0.8.7 */
    private static String get64LibraryMiddlePart() {
        if(isWindows)
            return "jcpuid-x86_64-windows";
        if(isKFreebsd)
            return "jcpuid-x86_64-kfreebsd";
        if(isFreebsd)
            return "jcpuid-x86_64-freebsd";
        if(isNetbsd)
            return "jcpuid-x86_64-netbsd";
        if(isOpenbsd)
            return "jcpuid-x86_64-openbsd";
        if(isMac){
            if(isX86){
                return "jcpuid-x86_64-osx";
            }
            // this will fail, we don't have any ppc libs, but we can't return null here.
            return "jcpuid-ppc_64-osx";
        }
        if(isSunos)
            return "jcpuid-x86_64-solaris";
        // use linux as the default, don't throw exception
        return "jcpuid-x86_64-linux";
    }

    private static String getLibrarySuffix()
    {
        if(isWindows)
            return ".dll";
        if(isMac)
            return ".jnilib";
        else
            return ".so";
    }
}
