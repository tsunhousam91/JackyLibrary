package com.jackylibrary.model;

import com.jackylibrary.JackyDBHelper;
import com.jackylibrary.LogUtils;
import com.jackylibrary.StringUtils;

import java.util.ArrayList;

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
     * 此方法務必於 JackyDBHelper.prepare() 呼叫前使用 否則無效
     *
     * @param columnName
     * @param dataType
     * @param isAllowNull
     */
    public synchronized void addColumnAndType(String columnName, JackyDBHelper.DataType dataType, boolean isAllowNull) {
        if (StringUtils.isEmpty(columnName)) {
            LogUtils.w(TAG, "addColumnAndType() failed: columnName is empty");
            return;
        }
        ColumnInfo columnInfo = new ColumnInfo(columnName, dataType, isAllowNull);
        if (columnInfos.contains(columnInfo)) {
            LogUtils.w(TAG, "addColumnAndType() failed: columnInfo already exist");
            return;
        }
        columnInfos.add(columnInfo);
    }

    public static class ColumnInfo {
        private String columnName;
        private JackyDBHelper.DataType dataType;
        boolean isAllowNull;

        public ColumnInfo(String columnName, JackyDBHelper.DataType dataType, boolean isAllowNull) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.isAllowNull = isAllowNull;
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
