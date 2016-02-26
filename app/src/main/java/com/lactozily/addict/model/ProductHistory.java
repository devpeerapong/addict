package com.lactozily.addict.model;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class ProductHistory extends RealmObject {
    private Date time;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
