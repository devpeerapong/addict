package com.lactozily.addict.model;

/**
 * Created by lactozily on 5/12/2016 AD.
 */
public class AddictApplicationInfo {
    private final String mProductName;
    private final String mPackageName;

    public AddictApplicationInfo(String productName, String packageName) {
        mPackageName = packageName;
        mProductName = productName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getProductName() {
        return mProductName;
    }
}
