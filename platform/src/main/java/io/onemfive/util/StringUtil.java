package io.onemfive.util;

public class StringUtil {

    public static String capitalizeFirst(String input) {
        char[] ic = input.toCharArray();
        char[] oc = new char[ic.length];
        int i=0;
        for(char c : ic) {
            if(i==0) {
                oc[i++] = Character.toUpperCase(c);
            } else {
                oc[i++] = c;
            }
        }
        return new String(oc);
    }

    public static String capitalize(String input) {
        char[] ic = input.toCharArray();
        char[] oc = new char[ic.length];
        int i=0;
        boolean next = false;
        for(char c : ic) {
            if(i==0 || next) {
                oc[i++] = Character.toUpperCase(c);
                next = false;
            } else if(c == ' ') {
                oc[i++] = c;
                next = true;
            } else {
                oc[i++] = c;
            }
        }
        return new String(oc);
    }

}
