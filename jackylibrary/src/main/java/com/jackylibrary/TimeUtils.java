package com.jackylibrary;

import android.content.ContentProvider;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class TimeUtils {

    private static HashMap<String, SimpleDateFormat> sdfMap = new HashMap<>();

    /**
     * 返回現在時間點的客製化時間格式
     * yyyy年 MM月 dd日 HH時 mm分 ss秒
     *
     * @param customDateFormat
     * @return
     */
    public static String getNowDateFormat(String customDateFormat) {
        return getDateFormat(customDateFormat, System.currentTimeMillis());
    }

    /**
     * 返回指定時間戳的客製化時間格式
     * yyyy年 MM月 dd日 HH時 mm分 ss秒
     *
     * @param customDateFormat
     * @param timeStamp
     * @return
     */
    public static String getDateFormat(String customDateFormat, long timeStamp) {
        SimpleDateFormat sdf = sdfMap.get(customDateFormat);
        if (sdf == null) {
            sdf = new SimpleDateFormat(customDateFormat, Locale.getDefault());
            sdfMap.put(customDateFormat, sdf);
        }
        return sdf.format(new Date(timeStamp));
    }
}
