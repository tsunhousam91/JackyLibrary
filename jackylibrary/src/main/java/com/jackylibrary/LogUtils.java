package com.jackylibrary;

import android.util.Log;

public class LogUtils {
    private static final String TAG = LogUtils.class.getName();
    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;

    /**
     * @param caller
     * @param msg
     * @param printType
     * @param needWriteFile 此參數若為 true 除了打印 log 訊息外， 也會將 log 寫入檔案進行儲存。
     */
    private static void printLog(Object caller, String msg, int printType, boolean needWriteFile) {
        String tag;
        if (caller != null) {
            //caller 是 String ，代表外部傳進來的 TAG 是一般的 String，而不是物件參照。
            tag = caller instanceof String ? (String) caller : caller.getClass().getName();
        } else {
            tag = TAG;
        }
        switch (printType) {
            case 1:
                Log.v(tag, msg);
                break;
            case 2:
                Log.d(tag, msg);
                break;
            case 3:
                Log.i(tag, msg);
                break;
            case 4:
                Log.w(tag, msg);
                break;
            case 5:
                Log.e(tag, msg);
                break;
        }
    }

    public static void v(Object caller, String msg) {
        printLog(caller, msg, VERBOSE, false);
    }

    public static void v(Object caller, int value) {
        printLog(caller, String.valueOf(value), VERBOSE, false);
    }

    public static void v(Object caller, long value) {
        printLog(caller, String.valueOf(value), VERBOSE, false);
    }

    public static void v(Object caller, float value) {
        printLog(caller, String.valueOf(value), VERBOSE, false);
    }

    public static void v(Object caller, double value) {
        printLog(caller, String.valueOf(value), VERBOSE, false);
    }

    public static void v(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), VERBOSE, false);
    }

    public static void v(Object caller, char value) {
        printLog(caller, String.valueOf(value), VERBOSE, false);
    }

    public static void d(Object caller, String msg) {
        printLog(caller, msg, DEBUG, false);
    }

    public static void d(Object caller, int value) {
        printLog(caller, String.valueOf(value), DEBUG, false);
    }

    public static void d(Object caller, long value) {
        printLog(caller, String.valueOf(value), DEBUG, false);
    }

    public static void d(Object caller, float value) {
        printLog(caller, String.valueOf(value), DEBUG, false);
    }

    public static void d(Object caller, double value) {
        printLog(caller, String.valueOf(value), DEBUG, false);
    }

    public static void d(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), DEBUG, false);
    }

    public static void d(Object caller, char value) {
        printLog(caller, String.valueOf(value), DEBUG, false);
    }

    public static void i(Object caller, String msg) {
        printLog(caller, msg, INFO, false);
    }

    public static void i(Object caller, int value) {
        printLog(caller, String.valueOf(value), INFO, false);
    }

    public static void i(Object caller, long value) {
        printLog(caller, String.valueOf(value), INFO, false);
    }

    public static void i(Object caller, float value) {
        printLog(caller, String.valueOf(value), INFO, false);
    }

    public static void i(Object caller, double value) {
        printLog(caller, String.valueOf(value), INFO, false);
    }

    public static void i(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), INFO, false);
    }

    public static void i(Object caller, char value) {
        printLog(caller, String.valueOf(value), INFO, false);
    }

    public static void w(Object caller, String msg) {
        printLog(caller, msg, WARN, false);
    }

    public static void w(Object caller, int value) {
        printLog(caller, String.valueOf(value), WARN, false);
    }

    public static void w(Object caller, long value) {
        printLog(caller, String.valueOf(value), WARN, false);
    }

    public static void w(Object caller, float value) {
        printLog(caller, String.valueOf(value), WARN, false);
    }

    public static void w(Object caller, double value) {
        printLog(caller, String.valueOf(value), WARN, false);
    }

    public static void w(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), WARN, false);
    }

    public static void w(Object caller, char value) {
        printLog(caller, String.valueOf(value), WARN, false);
    }

    public static void e(Object caller, String msg) {
        printLog(caller, msg, ERROR, false);
    }

    public static void e(Object caller, int value) {
        printLog(caller, String.valueOf(value), ERROR, false);
    }

    public static void e(Object caller, long value) {
        printLog(caller, String.valueOf(value), ERROR, false);
    }

    public static void e(Object caller, float value) {
        printLog(caller, String.valueOf(value), ERROR, false);
    }

    public static void e(Object caller, double value) {
        printLog(caller, String.valueOf(value), ERROR, false);
    }

    public static void e(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), ERROR, false);
    }

    public static void e(Object caller, char value) {
        printLog(caller, String.valueOf(value), ERROR, false);
    }

    public static void onlyLogV(Object caller, String msg) {
        printLog(caller, msg, VERBOSE, false);
    }

    public static void onlyLogD(Object caller, String msg) {
        printLog(caller, msg, DEBUG, false);
    }

    public static void onlyLogI(Object caller, String msg) {
        printLog(caller, msg, INFO, false);
    }

    public static void onlyLogW(Object caller, String msg) {
        printLog(caller, msg, WARN, false);
    }

    public static void onlyLogE(Object caller, String msg) {
        printLog(caller, msg, ERROR, false);
    }
}

