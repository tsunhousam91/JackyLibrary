package com.jackylibrary;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private static final String TAG = FileUtils.class.getName();

    public enum DirKind {
        // 不用宣告存取權限
        // 其他app無法存取 使用者也無法瀏覽 移除 APP 時會刪除
        // data/data/[packageName]/app_ + [外部傳進來的 name]
        // 注意 外面傳進來的 name 前面因為被強制加上 app_ 故無法用這個方式存取諸如 data/data/[packageName]/shared_prefs/ 等目錄
        // 想存取那些特殊目錄的話也行 但需要直接指定正確絕對路徑
        INNER_DIR,

        // 不用宣告存取權限
        // 其他app無法存取 使用者也無法瀏覽 移除 APP 時會刪除
        // data/data/[packageName]/files/
        FILES_DIR,

        // 不用宣告存取權限
        // 其他app無法存取 使用者也無法瀏覽 系統有可能會砍這邊的資料 移除 APP 時會刪除
        // data/data/[packageName]/cache/
        CACHE_DIR,

        // 任何APP 都可存取 但需要宣告權限 (Android 4.4之後 自己的APP 存取自己的 不用宣告權限)
        // 使用者可以瀏覽 移除 APP 時會刪除
        // storage/emulated/0/Android/data/[packageName]/files/
        EXTERNAL__FILES_DIR,

        // 任何APP 都可存取 但需要宣告權限 (Android 4.4之後 自己的APP 存取自己的 不用宣告權限)
        // 使用者可以瀏覽 系統有可能會砍這邊的資料 移除 APP 時會刪除
        // storage/emulated/0/Android/data/[packageName]/caches/
        EXTERNAL__CACHE_DIR,

        // 任何APP 都可存取 但需要宣告權限
        // 外部公共空間的根目錄
        // storage/emulated/0
        ENV_EXTERNAL__STORAGE_DIR,

        // 任何APP 都可存取 但需要宣告權限
        // 會根據你傳的參數決定，傳空字串就會跟 ENV_EXTERNAL__STORAGE_DIR 一樣
        // storage/emulated/0
        ENV_EXTERNAL__STORAGE_PUBLIC_DIR,
    }

    /**
     * 根據定義好的 DirKind 取得對應路徑的 File 物件
     *
     * @param dirKind
     * @param context   如果是 Env 開頭的 DirKind 不需要帶 context 直接帶 null 即可
     * @param extraInfo EXTERNAL__FILES_DIR 和 ENV_EXTERNAL__STORAGE_PUBLIC_DIR 會根據此參數回傳對應的 File
     * @return
     */
    public static File getDir(DirKind dirKind, Context context, String extraInfo) {
        if (dirKind.ordinal() < DirKind.ENV_EXTERNAL__STORAGE_DIR.ordinal()) {
            if (context == null) {
                return null;
            }
        }
        if (extraInfo == null) {
            extraInfo = "";
        }
        switch (dirKind) {
            case INNER_DIR:
                if (StringUtils.isNullOrEmpty(extraInfo)) {
                    LogUtils.w(TAG, "getDir() failed: extraInfo cannot be empty at DirKind-INNER_DIR");
                    return null;
                }
                return context.getDir(extraInfo, Context.MODE_PRIVATE);
            case FILES_DIR:
                return context.getFilesDir();
            case CACHE_DIR:
                return context.getCacheDir();
            case EXTERNAL__FILES_DIR:
                return context.getExternalFilesDir(extraInfo);
            case EXTERNAL__CACHE_DIR:
                return context.getExternalCacheDir();
            case ENV_EXTERNAL__STORAGE_DIR:
                return Environment.getExternalStorageDirectory();
            case ENV_EXTERNAL__STORAGE_PUBLIC_DIR:
                return Environment.getExternalStoragePublicDirectory(extraInfo);
        }
        return null;
    }

    /**
     * 這邊為了安全起見 嚴格限制外面傳進來的格式 一定要包含目錄跟檔名 這邊函數內部要做一些防呆處理
     * 回傳 true 當整個寫檔過程完全順利結束
     *
     * @param directory 寫入檔案的目錄
     * @param fileName  檔名
     * @param content   待寫入的內容
     */
    public static boolean writeFile(File directory, String fileName, String content) {
        boolean isSuccessful = false;
        if (directory == null) {
            LogUtils.onlyLogW(TAG, "writeFile() failed: directory is null");
            return false;
        }
        if (StringUtils.isNullOrEmpty(fileName)) {
            LogUtils.onlyLogW(TAG, "writeFile() failed: fileName is empty");
            return false;
        }
        if (!directory.isDirectory()) {
            //先試著把前面的父目錄全部建起來
            if (!directory.mkdirs()) {
                //建立的過程有可能會失敗，但注意，有可能前面一部分已經成功建起來，後面部分失敗。
                LogUtils.onlyLogW(TAG, "writeFile() failed: directory can not be created, directoryPath: "
                        + directory.getAbsolutePath());
                return false;
            }
        }
        File targetFile = new File(directory, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);
            fos.write(content.getBytes());
            fos.flush();
            isSuccessful = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    isSuccessful = false;
                }
            }
        }
        return isSuccessful;
    }

    public static String readFile(File sourceFile) {
        String result = "";
        if (sourceFile == null) {
            LogUtils.onlyLogW(TAG, "readFile() failed: sourceFile is null");
            return "";
        }
        if (!sourceFile.canRead()) {
            LogUtils.onlyLogW(TAG, "readFile() failed: sourceFile can not be read, filePath: "
                    + sourceFile.getAbsolutePath());
            return "";
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(sourceFile);
            byte[] bytes = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int len;
            while ((len = fis.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, len));
            }
            result = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    result = "";
                }
            }
        }
        return result;
    }

    /**
     * 這個複製檔案的方法並不回傳布林值 因為無法單從回傳值判定到底有無順利複製
     * 所以要注意 log 如果讀檔 或寫檔過程有發生任何意外 應該都會留下 log
     * 如果有產生檔案 但打開裡面都是空的 那有可能是讀檔失敗 所以回傳空字串
     *
     * @param sourceFile
     * @param targetDirectory
     * @param targetFileName
     */
    public static void copyFile(File sourceFile, File targetDirectory, String targetFileName) {
        File targetFile = new File(targetDirectory, targetFileName);
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) > 0) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            LogUtils.w(TAG, "copyFile() failed: file can not be found, sourcePath: "
                    + sourceFile.getAbsolutePath() + ", targetPath: " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            LogUtils.w(TAG, "copyFile() failed: IOException, " + e.getMessage());
        } finally {
            if (input != null ) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null ) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 此方法只能刪除一個檔案 且只能刪除檔案 無法刪除目錄 當你只想刪除檔案
     * 怕不小心刪除到目錄 請使用此方法
     *
     * @param targetFile
     */
    public static boolean deleteOneFile(File targetFile) {
        if (targetFile == null) {
            LogUtils.w(TAG, "deleteOneFile() failed: targetFile is null");
            return false;
        }
        // 如果不是目錄 才准許刪除
        if (targetFile.exists()) {
            if (targetFile.isDirectory()) {
                LogUtils.w(TAG, "deleteOneFile() failed: targetFile is directory, directoryPath: "
                        + targetFile.getAbsolutePath());
                return false;
            }
            if (!targetFile.delete()) {
                LogUtils.w(TAG, "deleteOneFile() failed: targetFile can not be deleted, filePath: "
                        + targetFile.getAbsolutePath());
                return false;
            }
            return true;
        } else {
            LogUtils.w(TAG, "deleteOneFile() failed: targetFile does not exist, filePath: "
                    + targetFile.getAbsolutePath());
            return false;
        }
    }


    /**
     * 此方法會遞迴地刪除該目標 及所有其下的子目標
     * 當你想刪除一整個目錄 包含目錄底下的檔案時 請用此方法
     * 另外此法也能刪除一般的檔案
     *
     * @param targetFile
     */
    public static boolean deleteFilesRecursively(File targetFile) {
        if (targetFile == null) {
            LogUtils.w(TAG, "deleteFilesRecursively() failed: targetFile is null");
            return false;
        }
        if (!targetFile.isDirectory()) {
            return deleteOneFile(targetFile);
        } else {
            File[] files = targetFile.listFiles();
            for (File file : files) {
                if (!deleteFilesRecursively(file)) {
                    return false;
                }
            }
            if (!targetFile.delete()) {
                LogUtils.w(TAG, "deleteFilesRecursively() failed: " +
                        "targetDirectory can not be deleted, directoryPath: "
                        + targetFile.getAbsolutePath());
                return false;
            }
            return true;
        }
    }

    /**
     * 檢查外部空間是否可被寫入
     * 注意 就算這邊回傳 true 只是代表外部空間狀態正常可以被寫入 但你想要寫入非自己package下的資料的話 還是需要請求權限
     *
     * @return
     */
    public static boolean isExternalWritable() {
        return Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
    }

    /**
     * 檢查外部空間是否可被讀取
     * 注意 就算這邊回傳 true 只是代表外部空間狀態正常可以被讀取 但你想要讀取非自己package下的資料的話 還是需要請求權限
     *
     * @return
     */
    public static boolean isExternalReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
