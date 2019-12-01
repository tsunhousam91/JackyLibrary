package com.jackylibrary.model;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.multidex.MultiDexApplication;
import android.util.Pair;

import com.jackylibrary.JackyDBHelper;
import com.jackylibrary.LogUtils;
import com.jackylibrary.PermissionHelper;

import java.util.ArrayList;

/**
 * 建議使用 JackyLibrary 的開發者可以自行寫一個 Application 繼承 JackyApplication
 * 或直接使用 JackyApplication 此類別會幫助您控管一些 JackyLibrary 的 Utils
 * 讓您更加方便的使用 當然如果您想完全手動管理這些 Utils 也是可以
 */
public class JackyApplication extends MultiDexApplication {

    private static final String TAG = JackyApplication.class.getName();
    private int activityStartedCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.prepare(this);
        PermissionHelper.prepare(this);
        ArrayList<Pair<Class<? extends JackyDao>, Class<? extends JackyEntity>>> requiredDaoEntityPairs = getRequiredDaoEntityPairs();
        if (requiredDaoEntityPairs != null) {
            for (Pair<Class<? extends JackyDao>, Class<? extends JackyEntity>> pair : requiredDaoEntityPairs) {
                JackyDBHelper.registerDao(pair.first);
                JackyDao dao = JackyDBHelper.getDao(pair.first);
                if (dao != null) {
                    dao.addColumnInfosByEntity(pair.second);
                }
            }
        }
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                LogUtils.d(TAG, activity.getClass().getName() + " onCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                LogUtils.d(TAG, activity.getClass().getName() + " onStarted");
                activityStartedCount++;
                if (activityStartedCount == 1) {
                    onChangeToForeground();
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                LogUtils.d(TAG, activity.getClass().getName() + " onResumed");
            }

            @Override
            public void onActivityPaused(Activity activity) {
                LogUtils.d(TAG, activity.getClass().getName() + " onPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                LogUtils.d(TAG, activity.getClass().getName() + " onStopped");
                activityStartedCount--;
                if (activityStartedCount == 0) {
                    onChangeToBackground();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                LogUtils.d(TAG, activity.getClass().getName() + " onSaveInstanceState");
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                LogUtils.d(TAG, activity.getClass().getName() + " onDestroyed");
            }
        });
    }

    /**
     * 這個方法被呼叫 代表接下來 app 將切換到前景
     * 如果有需要初始化的事件可以寫在此方法
     */
    @CallSuper
    public void onChangeToForeground() {
        LogUtils.d(TAG, "Application changes to the foreground");
    }

    /**
     * 這個方法被呼叫 代表接下來 app 將切換到背景 任何資源將不再安全 都有可能被系統殺掉
     * 如果有任何需要存儲或釋放的資源 可以寫在這邊
     */
    @CallSuper
    public void onChangeToBackground() {
        LogUtils.d(TAG, "Application changes to the background");
        LogUtils.flushLog();
        JackyDBHelper instance = JackyDBHelper.getInstance();
        if (instance != null) {
            instance.closeDBIfExist();
        }
    }


    /**
     * 這個方法會在 app onCreate 時被調用 用來 register 需要的 JackyDao 資訊
     * 開發者可以覆寫此方法 並回傳需要 < Dao.class, Entity.class > 對 的 ArrayList
     */
    public ArrayList<Pair<Class<? extends JackyDao>, Class<? extends JackyEntity>>> getRequiredDaoEntityPairs() {
        return null;
    }


}
