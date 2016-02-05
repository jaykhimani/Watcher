package com.jak.sandbox.watcher.helper;

public class Utils {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    private Utils() { /*Util class, should not be instantiated*/ }
}
