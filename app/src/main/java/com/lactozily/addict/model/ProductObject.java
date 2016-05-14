package com.lactozily.addict.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class ProductObject extends RealmObject {
    @PrimaryKey
    private String packageName;
    private String productName;
    private RealmList<ProductHistory> histories;
    private int counterDaily;
    private int counterMonthly;
    private int counterAllTime;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public RealmList<ProductHistory> getHistories() {
        return histories;
    }

    public int getCounterAllTime() {
        return counterAllTime;
    }

    public void setCounterAllTime(int counterAllTime) {
        this.counterAllTime = counterAllTime;
    }

    public int getCounterMonthly() {
        return counterMonthly;
    }

    public void setCounterMonthly(int counterMonthly) {
        this.counterMonthly = counterMonthly;
    }

    public int getCounterDaily() {
        return counterDaily;
    }

    public void setCounterDaily(int counterDaily) {
        this.counterDaily = counterDaily;
    }
}
