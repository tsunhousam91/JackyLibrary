package com.jackylibrary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.jackylibrary.model.JackyDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JackyDBHelper extends SQLiteOpenHelper {

    private static final String TAG = JackyDBHelper.class.getName();
    private static final String DEFAULT_NAME = "myDatabase";
    public static final String KEY_ID = "_id";
    private static final int DEFAULT_VERSION = 1;
    private static JackyDBHelper instance;
    private Context appContext;
    private static boolean isPrepared = false;

    /**
     * 此方法是一個接口 開發者如果想要自訂義 onUpgrade 的行為
     * 就要從外部定義好介面 onUpgradeListener 並把參數傳進來
     * 此方法務必要在 prepare() 之前呼叫 否則來不及生效
     *
     * @param listener
     */
    public static void setOnUpgradeListener(JackyDBHelper.onUpgradeListener listener) {
        onUpgradeListener = listener;
    }

    private static onUpgradeListener onUpgradeListener;

    public enum DataType {
        INTEGER("INTEGER"),
        REAL("REAL"),
        TEXT("TEXT");
        String name;

        DataType(String name) {
            this.name = name;
        }
    }

    private static HashMap<Class<JackyDao>, JackyDao> daoMap = new HashMap<>();


    /**
     * getInstance() 呼叫前 一定要先呼叫 prepare() 並把你的 context 資料庫 databaseName 還有資料庫版本 version 都帶進來
     * 當資料庫結構有變時 記得更新你的 version 通常是 +1
     * 建議將這個寫在您的 application 的 onCreate() 裡 進行初始化
     * 另外在呼叫 prepare() 前 請務必將您想要 register 的 Dao 先行 register 好
     *
     * @param context
     * @param databaseName
     * @param version
     */
    public static void prepare(Context context, String databaseName, int version) {
        if (isPrepared) {
            return;
        }
        synchronized (JackyDBHelper.class) {
            if (isPrepared) {
                return;
            }
            if (context == null) {
                LogUtils.w(TAG, "prepare() failed: context is null");
                return;
            }
            if (version < 1) {
                LogUtils.w(TAG, "prepare() failed: version must be greater than 1");
                return;
            }
            if (StringUtils.isEmpty(databaseName)) {
                databaseName = DEFAULT_NAME;
            }
            instance = new JackyDBHelper(context, databaseName, null, version);
            isPrepared = true;
        }
    }

    /**
     * 使用 getInstance() 前 請先呼叫 prepare()
     *
     * @return
     */
    public static JackyDBHelper getInstance() {
        if (!isPrepared) {
            LogUtils.w(TAG, "getInstance() failed: please call prepare() first");
            return null;
        }
        return instance;
    }

    /**
     * 這是用來註冊 Dao 的方法，建議您可在自訂義的 Application 的 onCreate 就把需要的 Dao 都先註冊好
     * 請務必在 prepare() 被呼叫前 先把所有該 register 的 Dao 都呼叫過 registerDao() 否則不會被建立
     *
     * @param daoClass
     */
    public static void registerDao(Class<JackyDao> daoClass) {
        if (daoClass == null) {
            LogUtils.w(TAG, "registerDao() failed: daoClass is null");
            return;
        }
        JackyDao jackyDao = daoMap.get(daoClass);
        if (jackyDao != null) {
            return;
        }
        try {
            jackyDao = daoClass.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "registerDao() failed: IllegalAccessException " + e.getMessage());
            return;
        } catch (InstantiationException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "registerDao() failed: InstantiationException " + e.getMessage());
            return;
        }
        daoMap.put(daoClass, jackyDao);
    }

    public static JackyDao getDao(Class<JackyDao> daoClass) {
        return daoMap.get(daoClass);
    }

    private JackyDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Map.Entry<Class<JackyDao>, JackyDao> classJackyDaoEntry : daoMap.entrySet()) {
            JackyDao dao = classJackyDaoEntry.getValue();
            Class<JackyDao> jackyDaoClass = classJackyDaoEntry.getKey();
            ArrayList<JackyDao.ColumnInfo> columnInfos = dao.getColumnInfos();
            if (columnInfos != null && columnInfos.size() > 0) {
                StringBuilder sbForSQL = new StringBuilder(
                        "CREATE TABLE " + jackyDaoClass.getSimpleName() + " (" +
                                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT");
                for (JackyDao.ColumnInfo columnInfo : columnInfos) {
                    sbForSQL.append(", ")
                            .append(columnInfo.getColumnName())
                            .append(" ")
                            .append(columnInfo.getDataType().name)
                            .append(columnInfo.isAllowNull() ? "" : " NOT　NULL");
                }
                sbForSQL.append(")");
                db.execSQL(sbForSQL.toString());
            }
        }
    }

    /**
     * 如果開發者有自己設定 onUpgradeListener
     * 則會使用開發者自訂的邏輯去走
     * 否則就用預設的邏輯 將舊的資料表全部刪除
     * 然後重建一次新的資料表
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (onUpgradeListener != null) {
            onUpgradeListener.onUpgrade(db, oldVersion, newVersion);
        } else {
            //下面會刪除db裡面所有的資料表
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type ='table' AND name != 'sqlite_sequence'", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    db.execSQL("DROP TABLE " + cursor.getString(0));
                    LogUtils.d(TAG, "delete table: " + cursor.getString(0));
                }
                cursor.close();
            }
            //然後呼叫 onCreate 重新建立資料表
            onCreate(db);
        }
    }

    public interface onUpgradeListener {
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }
}
