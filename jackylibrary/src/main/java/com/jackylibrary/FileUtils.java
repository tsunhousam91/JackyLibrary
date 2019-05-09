package com.jackylibrary;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = FileUtils.class.getName();

    public enum DirKind {
        // data/data/[packageName]/
        INNER_DIR,

        // 不用宣告存取權限
        // 其他app無法存取 使用者也無法瀏覽 移除 APP 時會刪除
        // data/data/[packageName]/files/
        FILES_DIR,

        // 不用宣告存取權限
        // 其他app無法存取 使用者也無法瀏覽 系統有可能會砍這邊的資料 移除 APP 時會刪除
        // data/data/[packageName]/cache/
        CACHE_DIR,

        // 任何APP 都可讀取 但需要宣告權限 (Android 4.4之後 自己的APP 存取自己的 不用宣告權限)
        // 能否寫入需測試 使用者可以瀏覽 系統有可能會砍這邊的資料 移除 APP 時會刪除
        // storage/emulated/0/Android/data/[packageName]/files/
        EXTERNAL__FILES_DIR,

        // 任何APP 都可讀取 但需要宣告權限 (Android 4.4之後 自己的APP 存取自己的 不用宣告權限)
        // 能否寫入需測試 使用者可以瀏覽 移除 APP 時會刪除
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
     * 這邊為了安全起見，嚴格限制外面傳進來的格式，一定要包含目錄跟檔名，這邊函數內部要做一些防呆處理。
     *
     * @param directory 寫入檔案的目錄
     * @param fileName  檔名
     * @param content   待寫入的內容
     */
    public static boolean writeFile(File directory, String fileName, String content) {
        if (directory == null) {
            LogUtils.onlyLogW(TAG, "writeFile() failed: directory is null");
            return false;
        }
        if (StringUtils.isEmpty(fileName)) {
            LogUtils.onlyLogW(TAG, "writeFile() failed: fileName is empty");
            return false;
        }
        if (!directory.exists() || !directory.isDirectory()) {
            //先試著把前面的父目錄全部建起來
            if (!directory.mkdirs()) {
                //建立的過程有可能會失敗，但注意，有可能前面一部分已經成功建起來，後面部分失敗。
                LogUtils.onlyLogW(TAG, "writeFile() failed: directory created failed");
                return false;
            }
        }
        File targetFile = new File(directory, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);
            fos.write(content.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 檢查外部空間是否可被寫入
     *
     * @return
     */
    public static boolean isExternalWritable() {
        return Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState());
    }

    /**
     * 檢查外部空間是否可被讀取
     *
     * @return
     */
    public static boolean isExternalReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
