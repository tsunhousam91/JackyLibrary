package com.jackylibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;


/**
 * Android 6.0 後，使用到危險權限時，必須主動向使用者要求存取權限，
 * 此工具可以幫您簡化向使用者要求權限時的步驟。
 */
public class PermissionHelper {
    public static final String READ_CALENDAR = Manifest.permission.READ_CALENDAR;
    public static final String WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR;
    public static final String CAMERA = Manifest.permission.CAMERA;
    public static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
    public static final String GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS;
    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final String READ_CALL_LOG = Manifest.permission.READ_CALL_LOG;
    public static final String WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG;
    public static final String ADD_VOICEMAIL = Manifest.permission.ADD_VOICEMAIL;
    public static final String USE_SIP = Manifest.permission.USE_SIP;
    public static final String PROCESS_OUTGOING_CALLS = Manifest.permission.PROCESS_OUTGOING_CALLS;
    public static final String BODY_SENSORS = Manifest.permission.BODY_SENSORS;
    public static final String SEND_SMS = Manifest.permission.SEND_SMS;
    public static final String RECEIVE_SMS = Manifest.permission.RECEIVE_SMS;
    public static final String READ_SMS = Manifest.permission.READ_SMS;
    public static final String RECEIVE_WAP_PUSH = Manifest.permission.RECEIVE_WAP_PUSH;
    public static final String RECEIVE_MMS = Manifest.permission.RECEIVE_MMS;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static Context appContext;

    private static class NotPreparedException extends RuntimeException {
        private NotPreparedException() {
            super("You must call prepare() before you can use PermissionHelper");
            printStackTrace();
        }
    }

    public static void prepare(Context context) {
        if (context == null) {
            throw new NullPointerException("prepare() failed: context is null");
        }
        appContext = context.getApplicationContext();
    }

    private static void checkPrepared() {
        if (appContext == null) {
            throw new NotPreparedException();
        }
    }

    /**
     * 確認是否擁有權限
     *
     * @param permission
     * @return
     */
    public static boolean isPermissionGranted(String permission) {
        checkPrepared();
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(appContext, permission);
    }

    /**
     * 可以請求一連串的權限，通常使用在想要一開始就跟使用者索取所有權限時使用。
     *
     * @param activity
     * @param permissions
     * @param requestCode
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * 此方法會檢查是否擁有權限，如果沒有，會自動幫你請求權限，並且如果已經曾經被拒絕過，
     * 會幫你跳出預設Dialog給使用者看，但你要傳入想顯示給使用者看的文字。
     */
    public static boolean defaultCheckPermissionAndRequest(final Activity activity, final String permission,
                                                           String hints, final int requestCode) {
        if (isPermissionGranted(permission)) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            //如果使用者曾拒絕過權限，就會進入這邊，跳出解釋視窗
            new AlertDialog.Builder(activity)
                    .setMessage(hints)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{permission},
                                    requestCode);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestCode);
        }
        return false;
    }

}
