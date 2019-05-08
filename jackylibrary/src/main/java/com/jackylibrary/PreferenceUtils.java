package com.jackylibrary;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class PreferenceUtils {

    private static HashMap<String, SharedPreferences> spMap;

    /**
     * 此方法回傳預設的 preference，將以 packageName 當檔名。
     *
     * @param context
     * @return
     */
    public static SharedPreferences getPreference(Context context) {
        if (context == null) {
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
        if (context == null || StringUtils.isEmpty(name)) {
            return null;
        }
        if (spMap == null) {
            spMap = new HashMap<>();
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
