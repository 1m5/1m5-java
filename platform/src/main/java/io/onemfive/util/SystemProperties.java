package io.onemfive.util;

import java.io.PrintStream;
import java.util.Properties;

/**
 * TODO: Add Description
 *
 */
public class SystemProperties {

    public static void printSystemProperties(PrintStream os) {
        Properties p = System.getProperties();
        for(Object key : p.keySet()) {
            os.println(key+":"+p.get(key));
        }
    }

//    public static void main(String[] args) {
//        printSystemProperties(System.out);
//    }
}
