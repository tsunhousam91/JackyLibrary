package com.jackylibrary;

import android.util.Log;

public class LogUtils {
    private static final String TAG = LogUtils.class.getName();
    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;

    private static void printLog(Object caller, String msg, int printType) {
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
        printLog(caller, msg, VERBOSE);
    }

    public static void v(Object caller, int value) {
        printLog(caller, String.valueOf(value), VERBOSE);
    }

    public static void v(Object caller, long value) {
        printLog(caller, String.valueOf(value), VERBOSE);
    }

    public static void v(Object caller, float value) {
        printLog(caller, String.valueOf(value), VERBOSE);
    }

    public static void v(Object caller, double value) {
        printLog(caller, String.valueOf(value), VERBOSE);
    }

    public static void v(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), VERBOSE);
    }

    public static void v(Object caller, char value) {
        printLog(caller, String.valueOf(value), VERBOSE);
    }

    public static void d(Object caller, String msg) {
        printLog(caller, msg, DEBUG);
    }

    public static void d(Object caller, int value) {
        printLog(caller, String.valueOf(value), DEBUG);
    }

    public static void d(Object caller, long value) {
        printLog(caller, String.valueOf(value), DEBUG);
    }

    public static void d(Object caller, float value) {
        printLog(caller, String.valueOf(value), DEBUG);
    }

    public static void d(Object caller, double value) {
        printLog(caller, String.valueOf(value), DEBUG);
    }

    public static void d(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), DEBUG);
    }

    public static void d(Object caller, char value) {
        printLog(caller, String.valueOf(value), DEBUG);
    }

    public static void i(Object caller, String msg) {
        printLog(caller, msg, INFO);
    }

    public static void i(Object caller, int value) {
        printLog(caller, String.valueOf(value), INFO);
    }

    public static void i(Object caller, long value) {
        printLog(caller, String.valueOf(value), INFO);
    }

    public static void i(Object caller, float value) {
        printLog(caller, String.valueOf(value), INFO);
    }

    public static void i(Object caller, double value) {
        printLog(caller, String.valueOf(value), INFO);
    }

    public static void i(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), INFO);
    }

    public static void i(Object caller, char value) {
        printLog(caller, String.valueOf(value), INFO);
    }

    public static void w(Object caller, String msg) {
        printLog(caller, msg, WARN);
    }

    public static void w(Object caller, int value) {
        printLog(caller, String.valueOf(value), WARN);
    }

    public static void w(Object caller, long value) {
        printLog(caller, String.valueOf(value), WARN);
    }

    public static void w(Object caller, float value) {
        printLog(caller, String.valueOf(value), WARN);
    }

    public static void w(Object caller, double value) {
        printLog(caller, String.valueOf(value), WARN);
    }

    public static void w(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), WARN);
    }

    public static void w(Object caller, char value) {
        printLog(caller, String.valueOf(value), WARN);
    }

    public static void e(Object caller, String msg) {
        printLog(caller, msg, ERROR);
    }

    public static void e(Object caller, int value) {
        printLog(caller, String.valueOf(value), ERROR);
    }

    public static void e(Object caller, long value) {
        printLog(caller, String.valueOf(value), ERROR);
    }

    public static void e(Object caller, float value) {
        printLog(caller, String.valueOf(value), ERROR);
    }

    public static void e(Object caller, double value) {
        printLog(caller, String.valueOf(value), ERROR);
    }

    public static void e(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), ERROR);
    }

    public static void e(Object caller, char value) {
        printLog(caller, String.valueOf(value), ERROR);
    }
}

