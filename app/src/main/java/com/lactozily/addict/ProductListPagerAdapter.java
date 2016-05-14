package com.lactozily.addict;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lactozily.addict.model.ProductObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by lactozily on 5/15/2016 AD.
 */
class ProductListPagerAdapter extends FragmentStatePagerAdapter {
    private static Realm realm;
    private static RealmResults<ProductObject> query;
    private static final String[] TAB_NAME = {"DAILY", "MONTHLY", "ALL TIME"};

    public ProductListPagerAdapter(FragmentManager fm, Realm r, RealmResults<ProductObject> q) {
        super(fm);
        realm = r;
        query = q;
    }

    @Override
    public Fragment getItem(int position) {
        SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
        String date_format;
        switch (position) {
            case 1:
                format = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
                date_format = "1 - " + Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) + " " + format.format(Calendar.getInstance().getTime()) ;
                break;
            case 2:
                date_format = "All time";
                break;
            default:
                date_format = format.format(Calendar.getInstance().getTime());
        }
        return ProductListFragment.newInstance(realm, query, position, date_format);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ProductListPagerAdapter.TAB_NAME[position];
    }
}
