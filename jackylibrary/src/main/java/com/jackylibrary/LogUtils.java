package com.jackylibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;

public class LogUtils {
    private static final String TAG = LogUtils.class.getName();
    private static final String LOG_CONFIG = "logConfig";
    private static final String ENABLE_LOG_TO_FILE = "enableLogToFile";
    private static final int VERBOSE = 1;
    private static final int DEBUG = 2;
    private static final int INFO = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final String[] logRanks = {"", "v ", "d ", "i ", "w ", "e "};
    private static Context appContext;
    private static File externalLogDirectory;
    private static File innerLogDirectory;
    private static final int DEFAULT_MAX_LOGS = 30;
    private static int maxLogsInBuffer = DEFAULT_MAX_LOGS;
    private static LogBuffer logBuffer = new LogBuffer();
    private static final String LOG_DIRECTORY_NAME = "logs";
    private static final String LOG_FILE_NAME_POSTFIX = "_log.txt";
    private static final String LOG_DATE_FORMAT = "yyyyMMdd";
    private static final String LOG_TIME_PREFIX_FORMAT = "HH:mm:ss ";
    private static final String NEW_LINE = "\r\n";
    private static String lastLogDate = "";

    private static class LogBuffer {
        private StringBuilder cacheLog;
        private int currentLogSize;

        private LogBuffer() {
            cacheLog = null;
            currentLogSize = 0;
        }

        /**
         * 宣告成 synchrinized 保護多線程同時要執行的問題
         *
         * @param tag
         * @param log
         */
        public synchronized void addNewLog(String tag, String log, String logRank) {
            long nowTimestamp = System.currentTimeMillis();
            String nowDate = TimeUtils.getDateFormat(LOG_DATE_FORMAT, nowTimestamp);

            if (cacheLog == null) {
                //會進入這邊就是第一次開 app
                String targetFileName = nowDate + LOG_FILE_NAME_POSTFIX;
                String oldLogs = readLogFromFile(targetFileName); //第一次開 app 需要把之前的 log 讀回來
                cacheLog = new StringBuilder(oldLogs);
                lastLogDate = nowDate;
            } else {
                if (!lastLogDate.equals(nowDate)) {
                    //上一筆 log 的日期跟這筆不一樣 進入換日了 要換新的檔案 要先把之前的 cacheLog 寫進檔案
                    //將 cacheLog 寫入檔案 並 reset logBuffer
                    String targetFileName = lastLogDate + LOG_FILE_NAME_POSTFIX;
                    writeLogToFile(targetFileName, cacheLog.toString());

                    //對 cacheLog 初始化 因為進入換日了 需要一個新的乾淨的緩存來放新的日期的 log
                    cacheLog = new StringBuilder();
                    currentLogSize = 0;
                    lastLogDate = nowDate;
                }
            }

            //將 log 資訊加進緩存 並讓 counter + 1
            cacheLog.append(TimeUtils.getDateFormat(LOG_TIME_PREFIX_FORMAT, nowTimestamp))
                    .append(logRank)
                    .append(tag)
                    .append(NEW_LINE)
                    .append(log)
                    .append(NEW_LINE);
            currentLogSize++;
            if (currentLogSize >= maxLogsInBuffer) {
                //將 cacheLog 寫入檔案 並清空 counter
                String targetFileName = nowDate + LOG_FILE_NAME_POSTFIX;
                writeLogToFile(targetFileName, cacheLog.toString());
                currentLogSize = 0;
            }
        }
    }


    public static void prepare(Context context) {
        if (context == null) {
            LogUtils.e(TAG, "prepare() failed: context is null");
            throw new NullPointerException("prepare() error: context is null");
        }
        appContext = context.getApplicationContext();
        File externalFilesDir = FileUtils.getDir(FileUtils.DirKind.EXTERNAL__FILES_DIR, appContext, null);
        externalLogDirectory = externalFilesDir != null ? new File(externalFilesDir.getParentFile(), LOG_DIRECTORY_NAME) : null;
        File innerFilesDir = FileUtils.getDir(FileUtils.DirKind.FILES_DIR, appContext, null);
        innerLogDirectory = innerFilesDir != null ? new File(innerFilesDir.getParentFile(), LOG_DIRECTORY_NAME) : null;
    }

    public static boolean isPrepared() {
        return appContext != null;
    }

    public static void setMaxLogsInBuffer(int maxBufferNumber) {
        if (maxBufferNumber > 0) {
            maxLogsInBuffer = maxBufferNumber;
        }
    }

    /**
     * @param caller
     * @param msg
     * @param printType
     * @param checkNeedWriteFile 此參數若為 true， 會檢查是否須將 log 寫入檔案進行儲存。
     */
    private static void printLog(Object caller, String msg, int printType, boolean checkNeedWriteFile) {
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
        if (checkNeedWriteFile && isEnableLogToFile()) {
            // 將新的 log 加到 buffer 並當滿足以下條件時呼叫 writeLogToFile 進行寫入
            // 1. 當發現日期變更時
            // 2. 當log 數抵達 maxLogsInBuffer (上限可調整)
            logBuffer.addNewLog(tag, msg, logRanks[printType]);
        }
    }

    /**
     * 此方法會將目前的 cacheLog 強制立即寫入檔案
     */
    public synchronized static void flushLog(){
        if(logBuffer.cacheLog != null){
            String nowDate = TimeUtils.getDateFormat(LOG_DATE_FORMAT, System.currentTimeMillis());
            String targetFileName = nowDate + LOG_FILE_NAME_POSTFIX;
            writeLogToFile(targetFileName, logBuffer.cacheLog.toString());
            logBuffer.currentLogSize = 0;
        }
    }

    /**
     * 此方法將 log 從檔案讀到 cahceLog
     *
     * @param fileName
     */
    private static String readLogFromFile(String fileName) {
        File targetDirectory;
        if (FileUtils.isExternalReadable() && externalLogDirectory != null) {
            targetDirectory = externalLogDirectory;
        } else {
            targetDirectory = innerLogDirectory;
        }
        return FileUtils.readFile(new File(targetDirectory, fileName));
    }

    /**
     * 此方法直接把訊息寫入到檔案
     * 這邊不再處理是否寫入成功或失敗的問題 理論上都會成功
     * 若失敗那可能是特殊底層問題我也無法處理 反正還是會有錯誤訊息
     *
     * @param fileName
     * @param msg
     */
    private static void writeLogToFile(String fileName, String msg) {
        File targetDirectory;
        if (FileUtils.isExternalWritable() && externalLogDirectory != null) {
            targetDirectory = externalLogDirectory;
        } else {
            targetDirectory = innerLogDirectory;
        }
        FileUtils.writeFile(targetDirectory, fileName, msg);
    }

    public static void v(Object caller, String msg) {
        printLog(caller, msg, VERBOSE, true);
    }

    public static void v(Object caller, int value) {
        printLog(caller, String.valueOf(value), VERBOSE, true);
    }

    public static void v(Object caller, long value) {
        printLog(caller, String.valueOf(value), VERBOSE, true);
    }

    public static void v(Object caller, float value) {
        printLog(caller, String.valueOf(value), VERBOSE, true);
    }

    public static void v(Object caller, double value) {
        printLog(caller, String.valueOf(value), VERBOSE, true);
    }

    public static void v(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), VERBOSE, true);
    }

    public static void v(Object caller, char value) {
        printLog(caller, String.valueOf(value), VERBOSE, true);
    }

    public static void d(Object caller, String msg) {
        printLog(caller, msg, DEBUG, true);
    }

    public static void d(Object caller, int value) {
        printLog(caller, String.valueOf(value), DEBUG, true);
    }

    public static void d(Object caller, long value) {
        printLog(caller, String.valueOf(value), DEBUG, true);
    }

    public static void d(Object caller, float value) {
        printLog(caller, String.valueOf(value), DEBUG, true);
    }

    public static void d(Object caller, double value) {
        printLog(caller, String.valueOf(value), DEBUG, true);
    }

    public static void d(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), DEBUG, true);
    }

    public static void d(Object caller, char value) {
        printLog(caller, String.valueOf(value), DEBUG, true);
    }

    public static void i(Object caller, String msg) {
        printLog(caller, msg, INFO, true);
    }

    public static void i(Object caller, int value) {
        printLog(caller, String.valueOf(value), INFO, true);
    }

    public static void i(Object caller, long value) {
        printLog(caller, String.valueOf(value), INFO, true);
    }

    public static void i(Object caller, float value) {
        printLog(caller, String.valueOf(value), INFO, true);
    }

    public static void i(Object caller, double value) {
        printLog(caller, String.valueOf(value), INFO, true);
    }

    public static void i(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), INFO, true);
    }

    public static void i(Object caller, char value) {
        printLog(caller, String.valueOf(value), INFO, true);
    }

    public static void w(Object caller, String msg) {
        printLog(caller, msg, WARN, true);
    }

    public static void w(Object caller, int value) {
        printLog(caller, String.valueOf(value), WARN, true);
    }

    public static void w(Object caller, long value) {
        printLog(caller, String.valueOf(value), WARN, true);
    }

    public static void w(Object caller, float value) {
        printLog(caller, String.valueOf(value), WARN, true);
    }

    public static void w(Object caller, double value) {
        printLog(caller, String.valueOf(value), WARN, true);
    }

    public static void w(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), WARN, true);
    }

    public static void w(Object caller, char value) {
        printLog(caller, String.valueOf(value), WARN, true);
    }

    public static void e(Object caller, String msg) {
        printLog(caller, msg, ERROR, true);
    }

    public static void e(Object caller, int value) {
        printLog(caller, String.valueOf(value), ERROR, true);
    }

    public static void e(Object caller, long value) {
        printLog(caller, String.valueOf(value), ERROR, true);
    }

    public static void e(Object caller, float value) {
        printLog(caller, String.valueOf(value), ERROR, true);
    }

    public static void e(Object caller, double value) {
        printLog(caller, String.valueOf(value), ERROR, true);
    }

    public static void e(Object caller, boolean value) {
        printLog(caller, String.valueOf(value), ERROR, true);
    }

    public static void e(Object caller, char value) {
        printLog(caller, String.valueOf(value), ERROR, true);
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

    /**
     * 此方法用來控制是否開啟 Log 時順便將結果寫入檔案的功能
     * 之所以用 commit 而不用 apply 是為了讓使用者知道是否開啟成功 或許會需要讓外部跳出提示訊息
     * 而且此方法並不是會被頻繁呼叫的方法 寫入的資訊也非常小 故選擇 commit
     *
     * @param isEnabled
     * @return
     */
    public static boolean enableLogToFile(boolean isEnabled) {
        if (!isPrepared()) {
            throw new NotPreparedException();
        }
        SharedPreferences sp = PreferenceUtils.getPreference(appContext, LOG_CONFIG);
        if (sp == null) {
            LogUtils.w(TAG, "enableLogToFile(" + isEnabled + ") failed: sp is null");
            return false;
        }
        if (sp.getBoolean(ENABLE_LOG_TO_FILE, false) != isEnabled) {
            if (!sp.edit().putBoolean(ENABLE_LOG_TO_FILE, isEnabled).commit()) {
                LogUtils.w(TAG, "enableLogToFile(" + isEnabled + ") failed: commit failed");
                return false;
            }
        }
        return true;
    }

    /**
     * 如果這邊出現非預期的情況 並無法簡單回傳 true false 處理 因為這樣會造成外部混淆
     * 所以這邊直接丟出例外
     *
     * @return
     */
    public static boolean isEnableLogToFile() {
        if (!isPrepared()) {
            throw new NotPreparedException();
        }
        SharedPreferences sp = PreferenceUtils.getPreference(appContext, LOG_CONFIG);
        if (sp == null) {
            LogUtils.e(TAG, "isEnableLogToFile() failed: sp is null");
            NullPointerException e = new NullPointerException("isEnableLogToFile() error: sp is null");
            e.printStackTrace();
            throw e;
        }
        return sp.getBoolean(ENABLE_LOG_TO_FILE, false);
    }

    private static class NotPreparedException extends RuntimeException {
        private NotPreparedException() {
            super("You must call prepare() before you can use writing logs function");
            printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (logBuffer.cacheLog != null && lastLogDate != null) {
            String targetFileName = lastLogDate + LOG_FILE_NAME_POSTFIX;
            writeLogToFile(targetFileName, logBuffer.cacheLog.toString());
        }
        super.finalize();
    }
}

