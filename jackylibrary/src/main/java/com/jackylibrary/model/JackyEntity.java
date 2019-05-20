package com.jackylibrary.model;


public class JackyEntity {
    private int _id;
    private String updateTime;

    public JackyEntity() {
        _id = 0;
        updateTime = null;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int get_id() {
        return _id;
    }

    public String getUpdateTime() {
        return updateTime;
    }
}
