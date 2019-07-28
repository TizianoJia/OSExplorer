package net.easydebug.osexplorer.util;

public class Utils {

    public static boolean isNotNull(String str) {
        if (str != null && str.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNull(String str) {
        return !isNotNull(str);
    }

}
