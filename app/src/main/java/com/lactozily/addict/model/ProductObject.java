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

    public void setHistories(RealmList<ProductHistory> histories) {
        this.histories = histories;
    }
}
