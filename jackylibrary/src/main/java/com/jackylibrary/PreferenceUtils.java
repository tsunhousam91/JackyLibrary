package com.jackylibrary;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class PreferenceUtils {
    private static final String TAG = PreferenceUtils.class.getName();
    private static HashMap<String, SharedPreferences> spMap = new HashMap<>();

    /**
     * 此方法回傳預設的 preference，將以 packageName 當檔名。
     *
     * @param context
     * @return
     */
    public static SharedPreferences getDefaultPreference(Context context) {
        if (context == null) {
            LogUtils.w(TAG, "getDefaultPreference() failed: context is null");
            return null;
        }
        return getPreference(context, context.getPackageName());
    }

    /**
     * 此方法會回傳你想要的 sharedPreference ，外部需傳入 context 和 檔名，格式不對就回傳 null。
     *
     * @param context
     * @param name
     * @return
     */
    public static SharedPreferences getPreference(Context context, String name) {
        if (context == null) {
            LogUtils.w(TAG, "getPreference() failed: context is null");
            return null;
        }
        if (StringUtils.isNullOrEmpty(name)) {
            LogUtils.w(TAG, "getPreference() abnormal: name is empty, will use defaultPreference");
            return getDefaultPreference(context);
        }
        SharedPreferences sp = spMap.get(name);
        if (sp != null) {
            return sp;
        }
        sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        spMap.put(name, sp);
        return sp;
    }

}
