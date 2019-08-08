package com.jackylibrary;

public class StringUtils {

    /**
     * 此方法會同時檢查null 或 空字串
     *
     * @param s
     * @return
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
