package com.jackylibrary;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

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
}
