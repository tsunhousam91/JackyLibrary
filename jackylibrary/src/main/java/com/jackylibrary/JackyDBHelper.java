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
    private static final String DEFAULT_NAME = "myDatabase.db";
    public static final String KEY_ID = "_id";
    public static final String UPDATE_TIME = "updateTime";
    private static JackyDBHelper instance;
    private Context appContext;
    private static boolean isPrepared = false;
    private SQLiteDatabase readableDatabase;
    private SQLiteDatabase writeableDatabase;

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (readableDatabase == null) {
            synchronized (JackyDBHelper.class) {
                if (readableDatabase == null) {
                    readableDatabase = super.getReadableDatabase();
                }
            }
        }
        return readableDatabase;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (writeableDatabase == null) {
            synchronized (JackyDBHelper.class) {
                if (writeableDatabase == null) {
                    writeableDatabase = super.getWritableDatabase();
                }
            }
        }
        return writeableDatabase;
    }

    /**
     * 此方法是一個接口 開發者如果想要自訂義 onUpgrade 的行為
     * 就要從外部定義好介面 onUpgradeListener 並把參數傳進來
     * 此方法建議在 prepare() 之前呼叫
     *
     * @param listener
     */
    public static void setOnUpgradeListener(JackyDBHelper.onUpgradeListener listener) {
        onUpgradeListener = listener;
    }

    /**
     * 此方法是一個接口 開發者如果想要自訂義 onTableInit 的行為
     * JackyDBHelper.onCreate() 的最後會自動調用此方法
     * 可用在將建立好的資料表初始化
     * 就要從外部定義好介面 onTableInitListener 並把參數傳進來
     * 此方法建議在 prepare() 之前呼叫
     *
     * @param listener
     */
    public static void setOnTableInitListener(JackyDBHelper.onTableInitListener listener) {
        onTableInitListener = listener;
    }

    private static onUpgradeListener onUpgradeListener;
    private static onTableInitListener onTableInitListener;

    public enum DataType {
        INTEGER("INTEGER"),
        REAL("REAL"),
        TEXT("TEXT");

        String name;

        DataType(String name) {
            this.name = name;
        }
    }

    private static HashMap<Class<? extends JackyDao>, JackyDao> daoMap = new HashMap<>();


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
            } else {
                if (databaseName.length() > 3 && !databaseName.substring(databaseName.length() - 3).equals(".db")) {
                    databaseName = databaseName + ".db";
                } else if (databaseName.equals(".db")) {
                    databaseName = DEFAULT_NAME;
                } else if (databaseName.length() <= 3) {
                    databaseName = databaseName + ".db";
                }
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
    public static void registerDao(Class<? extends JackyDao> daoClass) {
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

    public static JackyDao getDao(Class<? extends JackyDao> daoClass) {
        return daoMap.get(daoClass);
    }

    private JackyDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Map.Entry<Class<? extends JackyDao>, JackyDao> classJackyDaoEntry : daoMap.entrySet()) {
            JackyDao dao = classJackyDaoEntry.getValue();
            Class<? extends JackyDao> jackyDaoClass = classJackyDaoEntry.getKey();
            ArrayList<JackyDao.ColumnInfo> columnInfos = dao.getColumnInfos();
            if (columnInfos != null && columnInfos.size() > 0) {
                StringBuilder sbForSQL = new StringBuilder(
                        "CREATE TABLE " + jackyDaoClass.getSimpleName() + " (" +
                                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                UPDATE_TIME + " DATETIME NOT NULL DEFAULT (datetime('now','localtime'))");
                for (JackyDao.ColumnInfo columnInfo : columnInfos) {
                    sbForSQL.append(", ")
                            .append(columnInfo.getColumnName())
                            .append(" ")
                            .append(columnInfo.getDataType().name)
                            .append(columnInfo.isAllowNull() ? "" : " NOT　NULL")
                            .append(StringUtils.isEmpty(columnInfo.getDefaultValue()) ? "" : " DEFAULT " + columnInfo.getDefaultValue());
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
        if (onTableInitListener != null) {
            //若外部有定義初始化 table 的行為 則調用
            onTableInitListener.onTableInit(db);
        }
    }

    public interface onUpgradeListener {
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

    public interface onTableInitListener {
        void onTableInit(SQLiteDatabase db);
    }

    public void closeDBIfExist() {
        if (readableDatabase != null && readableDatabase.isOpen()) {
            readableDatabase.close();
        }
        if (writeableDatabase != null && writeableDatabase.isOpen()) {
            writeableDatabase.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        closeDBIfExist();
        super.finalize();
    }
}
