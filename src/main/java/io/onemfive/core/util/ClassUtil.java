package io.onemfive.core.util;

public class ClassUtil {
    public static String convertClassNameToResourcePath(String className) {
        return className.replace(".", "/");
    }
}
