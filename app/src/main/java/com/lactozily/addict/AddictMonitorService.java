package com.lactozily.addict;

import android.app.IntentService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.util.Log;

import com.lactozily.addict.model.ProductHistory;
import com.lactozily.addict.model.ProductObject;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class AddictMonitorService extends IntentService {
    public static final String TAG = AddictMonitorService.class.getSimpleName();
    public static final String NONE_PKG = "NONE_PKG";
    public static String CURRENT_APP;
    public static String PREVIOUS_APP;
    Realm realm;
    RealmResults<ProductObject> query;


    public AddictMonitorService() { super("addict-monitor-service"); }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Start Monitoring");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        realm = Realm.getDefaultInstance();
        query = realm.where(ProductObject.class).findAll();
        synchronized (this) {
            while (true) {
                try {
                    wait(700);
                    Log.i(TAG, "Monitoring");

                    if (AddictUtility.isScreenLocked(this)) {
                        Log.e(TAG, "Screen is locked");
                        CURRENT_APP = NONE_PKG;
                        continue;
                    }

                    CURRENT_APP = getCurrentPackage();
                    Log.i(TAG, "Current App: " + CURRENT_APP);

                    if (CURRENT_APP.equals(PREVIOUS_APP) || !hasPackage()) {
                        PREVIOUS_APP = CURRENT_APP;
                        Log.i(TAG, "SAME AS OR NO PKG");
                        Log.i(TAG, "Previous App: " + PREVIOUS_APP);
                        continue;
                    }

                    ProductObject productObject = query.where().equalTo("packageName", CURRENT_APP).findFirst();

                    realm.beginTransaction();
                    ProductHistory history = new ProductHistory();
                    history.setTime(Calendar.getInstance().getTime());
                    productObject.getHistories().add(history);

                    Calendar cToday = Calendar.getInstance();
                    cToday.set(Calendar.HOUR_OF_DAY, 0);
                    cToday.set(Calendar.MINUTE, 0);
                    cToday.set(Calendar.SECOND, 0);

                    Calendar cTmr = Calendar.getInstance();
                    cTmr.set(Calendar.HOUR_OF_DAY, 23);
                    cTmr.set(Calendar.MINUTE, 59);
                    cTmr.set(Calendar.SECOND, 59);
                    productObject.getHistories().where().between("time", cToday.getTime(), cTmr.getTime());
                    realm.commitTransaction();

                    PREVIOUS_APP = CURRENT_APP;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean hasPackage() {
        if (query.where().equalTo("packageName", CURRENT_APP).findAll().size() == 0)
            return false;

        return true;
    }

    private String getCurrentPackage(){
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService("usagestats");
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts-10000, ts);
        if (usageStats == null || usageStats.size() == 0) {
            return NONE_PKG;
        }
        Collections.sort(usageStats, new RecentUseComparator());
        return usageStats.get(0).getPackageName();
    }

    static class RecentUseComparator implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }
}
