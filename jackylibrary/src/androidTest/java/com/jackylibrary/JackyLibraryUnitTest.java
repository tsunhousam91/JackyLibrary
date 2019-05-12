package com.jackylibrary;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class JackyLibraryUnitTest {

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.jackylibrary.test", appContext.getPackageName());
    }

    @Test
    public void testFileUtils() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        LogUtils.d(this, "" + FileUtils.getDir(FileUtils.DirKind.FILES_DIR, appContext, null));
        LogUtils.d(this, "" + FileUtils.getDir(FileUtils.DirKind.CACHE_DIR, appContext, null));
        LogUtils.d(this, "" + FileUtils.getDir(FileUtils.DirKind.EXTERNAL__FILES_DIR, appContext, "123"));
        LogUtils.d(this, "" + FileUtils.getDir(FileUtils.DirKind.EXTERNAL__CACHE_DIR, appContext, null));
        LogUtils.d(this, "" + FileUtils.isExternalWritable());
        LogUtils.d(this, "" + FileUtils.isExternalReadable());
        LogUtils.d(this, "" + FileUtils.getDir(FileUtils.DirKind.ENV_EXTERNAL__STORAGE_DIR, appContext, null));
        LogUtils.d(this, "" + FileUtils.getDir(FileUtils.DirKind.ENV_EXTERNAL__STORAGE_PUBLIC_DIR, appContext, null));

        File innerFilesDir = FileUtils.getDir(FileUtils.DirKind.FILES_DIR, appContext, "");
        File innerCacheDir = FileUtils.getDir(FileUtils.DirKind.CACHE_DIR, appContext, "");
        File externalFilesDir = FileUtils.getDir(FileUtils.DirKind.EXTERNAL__FILES_DIR, appContext, "");
        File externalCacheDir = FileUtils.getDir(FileUtils.DirKind.EXTERNAL__CACHE_DIR, appContext, "");
        FileUtils.writeFile(innerFilesDir, "testFile.txt", "this is a test.");
        FileUtils.writeFile(innerCacheDir, "testFile.txt", "this is a test.");
        FileUtils.writeFile(externalFilesDir, "testFile.txt", "this is a test.");
        FileUtils.writeFile(externalCacheDir, "testFile.txt", "this is a test.");
        for (int i = 0; i < 15; i++) {
            FileUtils.copyFile(new File(externalFilesDir, "testFile.txt"), externalFilesDir, i + ".txt");
        }
        FileUtils.deleteOneFile(new File(externalFilesDir, "0.txt"));
        FileUtils.deleteOneFile(new File(externalFilesDir, "123"));
        FileUtils.deleteOneFile(new File(externalFilesDir, "11.txt"));

        FileUtils.deleteFilesRecursively(new File(externalFilesDir, "123"));
        FileUtils.deleteFilesRecursively(new File(externalFilesDir, "0.txt"));
        FileUtils.deleteFilesRecursively(externalFilesDir);

        return;
    }

    @Test
    public void testLogUtils() {
        for (int i = 1; i <= 5; i++) {
            switch (i) {
                case 1:
                    LogUtils.v(this, 123);
                    LogUtils.v(this, 123L);
                    LogUtils.v(this, 123.);
                    LogUtils.v(this, (double) 123);
                    LogUtils.v(this, 'A');
                    LogUtils.v(this, true);
                    LogUtils.v("String Tag", "this is String by String TAG");
                    break;
                case 2:
                    LogUtils.d(this, 123);
                    LogUtils.d(this, 123L);
                    LogUtils.d(this, 123.);
                    LogUtils.d(this, (double) 123);
                    LogUtils.d(this, 'A');
                    LogUtils.d(this, true);
                    LogUtils.d("String Tag", "this is String by String TAG");
                    break;
                case 3:
                    LogUtils.i(this, 123);
                    LogUtils.i(this, 123L);
                    LogUtils.i(this, 123.);
                    LogUtils.i(this, (double) 123);
                    LogUtils.i(this, 'A');
                    LogUtils.i(this, true);
                    LogUtils.i("String Tag", "this is String by String TAG");
                    break;
                case 4:
                    LogUtils.w(this, 123);
                    LogUtils.w(this, 123L);
                    LogUtils.w(this, 123.);
                    LogUtils.w(this, (double) 123);
                    LogUtils.w(this, 'A');
                    LogUtils.w(this, true);
                    LogUtils.w("String Tag", "this is String by String TAG");
                    break;
                case 5:
                    LogUtils.e(this, 123);
                    LogUtils.e(this, 123L);
                    LogUtils.e(this, 123.);
                    LogUtils.e(this, (double) 123);
                    LogUtils.e(this, 'A');
                    LogUtils.e(this, true);
                    LogUtils.e("String Tag", "this is String by String TAG");
                    break;
            }
        }
    }

    @Test
    public void testPreferenceUtils() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        PreferenceUtils.getPreference(appContext, "hello").edit().putBoolean("test", true).commit();
        PreferenceUtils.getPreference(appContext, null).edit().putBoolean("test", true).commit();
        FileUtils.deleteOneFile(
                new File("/data/user/0/com.jackylibrary.test/shared_prefs/com.jackylibrary.test.xml"));
        PreferenceUtils.getPreference(appContext, "").edit().putBoolean("test", true).commit();
        FileUtils.deleteOneFile(
                new File("/data/user/0/com.jackylibrary.test/shared_prefs/com.jackylibrary.test.xml"));
    }
}
