package com.jackylibrary.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.jackylibrary.JackyDBHelper;
import com.jackylibrary.LogUtils;
import com.jackylibrary.StringUtils;
import com.jackylibrary.TimeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static com.jackylibrary.JackyDBHelper.KEY_ID;
import static com.jackylibrary.JackyDBHelper.UPDATE_TIME;

/**
 * 開發者可以自行繼承此 Dao 但請不要設定一樣的 className (SimpleName)
 * 一樣的 SimpleName 會導致 table 撞名
 */
public class JackyDao {

    private static final String TAG = JackyDao.class.getName();
    private ArrayList<ColumnInfo> columnInfos = new ArrayList<>();

    /**
     * 此方法會在 JackyDBHelper 的 onCreate() 中被調用
     * 在其 onCreate() 方法中需要知道該建立資料表的內部需要的欄位格式資訊
     * 開發者通常不需要覆寫此方法  只需要在 JackyDBHelper.prepare() 呼叫前先呼叫 JackyDao.addColumnAndType()
     * 將需要的欄位資訊傳遞進去 後續工作 Library 會協助完成
     *
     * @return
     */
    public ArrayList<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    /**
     * 此方法用來讓開發者 自行新增需要的欄位
     * 此方法建議於 JackyDBHelper.prepare() 呼叫前使用
     * 注意 關於 isAllowNull 參數 似乎沒有作用 還是可以塞入 null 值
     * 但總之先不要依賴他 記得還是都要塞值就不會有問題了
     *
     * @param columnName
     * @param dataType
     * @param isAllowNull
     */
    public synchronized void addColumnAndType(String columnName, JackyDBHelper.DataType dataType, boolean isAllowNull, String defaultValue) {
        if (StringUtils.isNullOrEmpty(columnName)) {
            LogUtils.w(this, "addColumnAndType() failed: columnName is empty");
            return;
        }
        ColumnInfo columnInfo = new ColumnInfo(columnName, dataType, isAllowNull, defaultValue);
        if (columnInfos.contains(columnInfo)) {
            LogUtils.w(this, "addColumnAndType() failed: columnInfo already exist");
            return;
        }
        columnInfos.add(columnInfo);
    }


    public void addColumnInfosByEntity(Class<? extends JackyEntity> childClass) {
        if (childClass == null) {
            LogUtils.w(this,
                    "addColumnInfosByEntity() failed: childClass is null");
            return;
        }
        if (childClass == JackyEntity.class) {
            LogUtils.w(this,
                    "addColumnInfosByEntity() failed: you should extends JackyEntity instead of using it directly");
            return;
        }
        Field[] childFields = childClass.getDeclaredFields();
        for (Field field : childFields) {
            if(field.isSynthetic()){
                //忽略編譯階段產生的field
                continue;
            }
            if("serialVersionUID".equals(field.getName())){
                //忽略serialVersionUID
                continue;
            }
            JackyDBHelper.DataType dataType = getDataTypeByField(field);
            if (dataType == null) {
                continue;
            }
            addColumnAndType(field.getName(), dataType);
        }
    }

    private JackyDBHelper.DataType getDataTypeByField(Field field) {
        String typeName = field.getType().getSimpleName();
        switch (typeName) {
            case "int":
            case "long":
            case "byte":
            case "short":
                return JackyDBHelper.DataType.INTEGER;
            case "float":
            case "double":
                return JackyDBHelper.DataType.REAL;
            case "String":
                return JackyDBHelper.DataType.TEXT;
        }
        return null;
    }

    /**
     * 插入一筆新的資料到資料表
     * 回傳 -1 代表插入錯誤
     * 否則回傳最近一筆插入資料的 ID
     *
     * @param columnNames
     * @param values
     * @return
     */
    public long insertData(String[] columnNames, String[] values) {
        if (columnNames == null || values == null || columnNames.length != values.length) {
            LogUtils.w(this, "insertData() failed: invalid columnNames or values");
            return -1;
        }
        JackyDBHelper instance = JackyDBHelper.getInstance();
        if (instance == null) {
            LogUtils.w(this, "insertData() failed: JackyDBHelper instance is null");
            return -1;
        }
        ContentValues cv = new ContentValues();
        for (int i = 0; i < columnNames.length; i++) {
            cv.put(columnNames[i], values[i]);
        }
        return instance.getWritableDatabase().insert(getClass().getSimpleName(), null, cv);
    }

    /**
     * 此方法用來更新資料表的欄位資訊
     * searchColumnNames 可填入想查找的欄位 searchValues 則是想查找的欄位鎖定的目標值
     * 注意 此方法只能用來查找特定欄位中符合您想搜尋的值的項目
     * 不支援更複雜的搜尋 (例如 某欄位的資料是否大於 或小於某個數值等等) 這邊只支援 等於
     * 想要更複雜的搜尋就要自己呼叫 db.update() 並手動填入您想搜尋的參數
     * 或者直接呼叫 db.execSQL()
     * 回傳被更新的項目數
     *
     * @param searchColumnNames
     * @param searchValues
     * @param updateColumnNames
     * @param updateValues
     * @return
     */
    public int updateData(String[] searchColumnNames, String[] searchValues, String[] updateColumnNames, String[] updateValues) {
        if (searchColumnNames == null || searchValues == null || searchColumnNames.length != searchValues.length || searchColumnNames.length == 0) {
            LogUtils.w(this, "updateData() failed: invalid searchColumnNames or searchValues");
            return 0;
        }
        if (updateColumnNames == null || updateValues == null || updateColumnNames.length != updateValues.length || updateColumnNames.length == 0) {
            LogUtils.w(this, "updateData() failed: invalid updateColumnNames or updateValues");
            return 0;
        }
        JackyDBHelper instance = JackyDBHelper.getInstance();
        if (instance == null) {
            LogUtils.w(this, "updateData() failed: JackyDBHelper instance is null");
            return 0;
        }
        ContentValues cv = new ContentValues();
        cv.put(JackyDBHelper.UPDATE_TIME, TimeUtils.getNowDateFormat("yyyy-MM-dd HH:mm:ss"));
        for (int i = 0; i < updateColumnNames.length; i++) {
            cv.put(updateColumnNames[i], updateValues[i]);
        }
        StringBuilder sbForWhere = new StringBuilder();
        sbForWhere.append(searchColumnNames[0])
                .append("=?");
        for (int i = 1; i < searchColumnNames.length; i++) {
            sbForWhere.append(" AND ")
                    .append(searchColumnNames[i])
                    .append("=?");
        }
        return instance.getWritableDatabase().update(getClass().getSimpleName(), cv, sbForWhere.toString(), searchValues);
    }

    /**
     * 此方法用來刪除資料表的項目
     * searchColumnNames 可填入想查找的欄位 searchValues 則是想查找的欄位鎖定的目標值
     * 注意 此方法只能用來查找特定欄位中符合您想搜尋的值的項目
     * 不支援更複雜的搜尋 (例如 某欄位的資料是否大於 或小於某個數值等等) 這邊只支援 等於
     * 想要更複雜的搜尋就要自己呼叫 db.delete() 並手動填入您想搜尋的參數
     * 或者直接呼叫 db.execSQL()
     * 回傳被刪除的項目數
     *
     * @param searchColumnNames
     * @param searchValues
     * @return
     */
    public int deleteData(String[] searchColumnNames, String[] searchValues) {
        if (searchColumnNames == null || searchValues == null || searchColumnNames.length != searchValues.length || searchColumnNames.length == 0) {
            LogUtils.w(this, "deleteData() failed: invalid searchColumnNames or searchValues");
            return 0;
        }
        JackyDBHelper instance = JackyDBHelper.getInstance();
        if (instance == null) {
            LogUtils.w(this, "deleteData() failed: JackyDBHelper instance is null");
            return 0;
        }
        StringBuilder sbForWhere = new StringBuilder();
        sbForWhere.append(searchColumnNames[0])
                .append("=?");
        for (int i = 1; i < searchColumnNames.length; i++) {
            sbForWhere.append(" AND ")
                    .append(searchColumnNames[i])
                    .append("=?");
        }
        return instance.getWritableDatabase().delete(getClass().getSimpleName(), sbForWhere.toString(), searchValues);
    }

    /**
     * 此方法用來刪除資料表所有的項目
     * 回傳被刪除的項目數
     *
     * @return
     */
    public int deleteAllData() {
        JackyDBHelper instance = JackyDBHelper.getInstance();
        if (instance == null) {
            LogUtils.w(this, "deleteAllData() failed: JackyDBHelper instance is null");
            return 0;
        }
        return instance.getWritableDatabase().delete(getClass().getSimpleName(), "1", null);
    }


    /**
     * 此方法用來查詢資料表的項目
     * 會回傳對應型態的ArrayList
     *
     * @param searchColumnNames
     * @param searchValues
     * @param childClass
     * @return
     */
    public ArrayList<? extends JackyEntity> queryData(String[] searchColumnNames, String[] searchValues, Class<? extends JackyEntity> childClass) {
        if (childClass == JackyEntity.class) {
            LogUtils.w(this,
                    "queryData() failed: you should extends JackyEntity instead of using it directly");
            return null;
        }
        if (searchColumnNames == null || searchValues == null || searchColumnNames.length != searchValues.length || searchColumnNames.length == 0) {
            LogUtils.w(this, "queryData() failed: invalid searchColumnNames or searchValues");
            return null;
        }
        JackyDBHelper instance = JackyDBHelper.getInstance();
        if (instance == null) {
            LogUtils.w(this, "queryData() failed: JackyDBHelper instance is null");
            return null;
        }
        StringBuilder sbForSql = new StringBuilder("SELECT * FROM " + getClass().getSimpleName()
                + " WHERE " + searchColumnNames[0] + " =?");
        for (int i = 1; i < searchColumnNames.length; i++) {
            sbForSql.append(" AND ")
                    .append(searchColumnNames[i])
                    .append(" =?");
        }
        Cursor cursor = instance.getReadableDatabase().rawQuery(sbForSql.toString(), searchValues);
        if (cursor == null) {
            LogUtils.w(this, "queryData() failed: cursor is null");
            return null;
        }
        ArrayList<JackyEntity> entities = null;
        try {
            if (cursor.moveToFirst()) {
                entities = new ArrayList<>();
                Field[] childFields = childClass.getDeclaredFields();
                do {
                    JackyEntity entity;
                    try {
                        entity = childClass.newInstance();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        LogUtils.e(this, "queryData() error: " + e.getMessage());
                        return null;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        LogUtils.e(this, "queryData() error: " + e.getMessage());
                        return null;
                    }
                    for (Field field : childFields) {
                        int columnIndex = cursor.getColumnIndex(field.getName());
                        if (columnIndex == -1) {
                            continue;
                        }
                        String setterName = getSetterNameForFieldName(field.getName());
                        if (StringUtils.isNullOrEmpty(setterName)) {
                            LogUtils.w(this, "queryData() failed: setterName is empty");
                            return null;
                        }
                        String typeName = field.getType().getSimpleName();
                        try {
                            switch (typeName) {
                                case "int":
                                    childClass.getDeclaredMethod(setterName, int.class)
                                            .invoke(entity, cursor.getInt(columnIndex));
                                    break;
                                case "long":
                                    childClass.getDeclaredMethod(setterName, long.class)
                                            .invoke(entity, cursor.getLong(columnIndex));
                                    break;
                                case "byte":
                                    childClass.getDeclaredMethod(setterName, byte.class)
                                            .invoke(entity, (byte) cursor.getInt(columnIndex));
                                    break;
                                case "short":
                                    childClass.getDeclaredMethod(setterName, short.class)
                                            .invoke(entity, cursor.getShort(columnIndex));
                                    break;
                                case "float":
                                    childClass.getDeclaredMethod(setterName, float.class)
                                            .invoke(entity, cursor.getFloat(columnIndex));
                                    break;
                                case "double":
                                    childClass.getDeclaredMethod(setterName, double.class)
                                            .invoke(entity, cursor.getDouble(columnIndex));
                                    break;
                                case "String":
                                    childClass.getDeclaredMethod(setterName, String.class)
                                            .invoke(entity, cursor.getString(columnIndex));
                                    break;
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                            LogUtils.e(this, "queryData() error: " + e.getMessage());
                            return null;
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            LogUtils.e(this, "queryData() error: " + e.getMessage());
                            return null;
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                            LogUtils.e(this, "queryData() error: " + e.getMessage());
                            return null;
                        }
                    }
                    entity.set_id(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                    entity.setUpdateTime(cursor.getString(cursor.getColumnIndex(UPDATE_TIME)));
                    entities.add(entity);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return entities;
    }


    /**
     * 此方法用來查詢資料表所有的項目
     * 會回傳對應型態的ArrayList
     *
     * @param childClass
     * @return
     */
    public ArrayList<? extends JackyEntity> queryAllData(Class<? extends JackyEntity> childClass) {
        if (childClass == JackyEntity.class) {
            LogUtils.w(this,
                    "queryData() failed: you should extends JackyEntity instead of using it directly");
            return null;
        }
        JackyDBHelper instance = JackyDBHelper.getInstance();
        if (instance == null) {
            LogUtils.w(this, "queryData() failed: JackyDBHelper instance is null");
            return null;
        }
        String sql = "SELECT * FROM " + getClass().getSimpleName();
        Cursor cursor = instance.getReadableDatabase().rawQuery(sql, null);
        if (cursor == null) {
            LogUtils.w(this, "queryData() failed: cursor is null");
            return null;
        }
        ArrayList<JackyEntity> entities = null;
        entities = new ArrayList<>();
        Field[] childFields = childClass.getDeclaredFields();
        try {
            while (cursor.moveToNext()) {
                JackyEntity entity;
                try {
                    entity = childClass.newInstance();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    LogUtils.e(this, "queryData() error: " + e.getMessage());
                    return null;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    LogUtils.e(this, "queryData() error: " + e.getMessage());
                    return null;
                }
                for (Field field : childFields) {
                    int columnIndex = cursor.getColumnIndex(field.getName());
                    if (columnIndex == -1) {
                        continue;
                    }
                    String setterName = getSetterNameForFieldName(field.getName());
                    if (StringUtils.isNullOrEmpty(setterName)) {
                        LogUtils.w(this, "queryData() failed: setterName is empty");
                        return null;
                    }
                    String typeName = field.getType().getSimpleName();
                    try {
                        switch (typeName) {
                            case "int":
                                childClass.getDeclaredMethod(setterName, int.class)
                                        .invoke(entity, cursor.getInt(columnIndex));
                                break;
                            case "long":
                                childClass.getDeclaredMethod(setterName, long.class)
                                        .invoke(entity, cursor.getLong(columnIndex));
                                break;
                            case "byte":
                                childClass.getDeclaredMethod(setterName, byte.class)
                                        .invoke(entity, (byte) cursor.getInt(columnIndex));
                                break;
                            case "short":
                                childClass.getDeclaredMethod(setterName, short.class)
                                        .invoke(entity, cursor.getShort(columnIndex));
                                break;
                            case "float":
                                childClass.getDeclaredMethod(setterName, float.class)
                                        .invoke(entity, cursor.getFloat(columnIndex));
                                break;
                            case "double":
                                childClass.getDeclaredMethod(setterName, double.class)
                                        .invoke(entity, cursor.getDouble(columnIndex));
                                break;
                            case "String":
                                childClass.getDeclaredMethod(setterName, String.class)
                                        .invoke(entity, cursor.getString(columnIndex));
                                break;
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                        LogUtils.e(this, "queryData() error: " + e.getMessage());
                        return null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        LogUtils.e(this, "queryData() error: " + e.getMessage());
                        return null;
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        LogUtils.e(this, "queryData() error: " + e.getMessage());
                        return null;
                    }
                }
                entity.set_id(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                entity.setUpdateTime(cursor.getString(cursor.getColumnIndex(UPDATE_TIME)));
                entities.add(entity);
            }
        } finally {
            cursor.close();
        }
        return entities;
    }

    private String getSetterNameForFieldName(String fieldName) {
        if (StringUtils.isNullOrEmpty(fieldName)) {
            return null;
        }
        if (fieldName.length() == 1) {
            return "set" + fieldName.toUpperCase();
        }
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    /**
     * 此方法用來讓開發者 自行新增需要的欄位
     * 預設 不允許欄位存在 NULL 值
     * 此方法建議於 JackyDBHelper.prepare() 呼叫前使用
     *
     * @param columnName
     * @param dataType
     */
    public synchronized void addColumnAndType(String columnName, JackyDBHelper.DataType dataType) {
        String defaultValue = null;
        switch (dataType) {
            case INTEGER:
            case REAL:
                defaultValue = "0";
                break;
            case TEXT:
                defaultValue = "\"\"";
                break;
        }
        addColumnAndType(columnName, dataType, false, defaultValue);
    }

    public static class ColumnInfo {
        private String columnName;
        private JackyDBHelper.DataType dataType;
        private boolean isAllowNull;
        private String defaultValue;

        public ColumnInfo(String columnName, JackyDBHelper.DataType dataType, boolean isAllowNull, String defaultValue) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.isAllowNull = isAllowNull;
            this.defaultValue = defaultValue;
        }

        public String getColumnName() {
            return columnName;
        }

        public JackyDBHelper.DataType getDataType() {
            return dataType;
        }

        public boolean isAllowNull() {
            return isAllowNull;
        }

        public String getDefaultValue() {
            return defaultValue;
        }


        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ColumnInfo)) {
                return false;
            }
            ColumnInfo columnInfo = (ColumnInfo) obj;
            if (columnInfo.columnName == null) {
                return false;
            }
            return columnInfo.columnName.equals(this.columnName);
        }

        @Override
        public int hashCode() {
            return this.columnName == null ? -1 : this.columnName.length();
        }
    }
}
