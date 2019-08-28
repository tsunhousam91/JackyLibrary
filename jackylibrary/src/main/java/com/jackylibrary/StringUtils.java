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

    public static boolean equals(String s1, String s2){
        if(s1 == s2){
            return true;
        }else if(s1 != null && s2!=null){
            return isStringMatch(false, s2, s2);
        }else {
            return false;
        }
    }

    public static boolean equalsIgnoreCase(String s1, String s2){
        if(s1 == s2){
            return true;
        }else if(s1 != null && s2!=null){
            return isStringMatch(true, s2, s2);
        }else {
            return false;
        }
    }

    /**
     * 比較兩個非null 的字串 是否相等
     * @param ignoreCase 是否忽略大小寫
     * @param s1 比較字串1
     * @param s2 比較字串2
     * @return
     */
    private static boolean isStringMatch(boolean ignoreCase, String s1,
                                 String s2) {
        if(s1.length() != s2.length()){
            return false;
        }
        for(int i=0; i<s1.length(); i++){
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 == c2) {
                continue;
            }
            if (ignoreCase) {
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if (u1 == u2) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }
}
