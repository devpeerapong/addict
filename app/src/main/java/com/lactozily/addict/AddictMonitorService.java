package com.lactozily.addict;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
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

                    Calendar cToday = AddictUtility.getStartTimeOfDate();
                    Calendar cTmr = AddictUtility.getEndTimeOfDate();
                    Calendar firstDateOfMonth = AddictUtility.getStartTimeOfMonth();
                    Calendar lastDateOfMonth = AddictUtility.getEndTimeOfMonth();

                    realm.beginTransaction();
                    ProductHistory history = new ProductHistory();
                    history.setTime(Calendar.getInstance().getTime());
                    productObject.getHistories().add(history);
                    productObject.setCounterAllTime(productObject.getHistories().size());
                    int counter = productObject.getHistories().where().between("time", cToday.getTime(), cTmr.getTime()).findAll().size();
                    int mCounter = productObject.getHistories().where().between("time", firstDateOfMonth.getTime(), lastDateOfMonth.getTime()).findAll().size();
                    productObject.setCounterDaily(counter);
                    productObject.setCounterMonthly(mCounter);
                    realm.commitTransaction();

                    checkUsage(counter, productObject.getProductName());
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

    private void checkUsage(int counter, String name) {
        String text = name + " " + String.valueOf(counter) + " time today.";
        switch (counter) {
            case 20:
                notifyUsage(counter, name, "Be careful!", text);
                break;
            case 50:
                notifyUsage(counter, name, "Please, go get a life!", text);
                break;
            case 80:
                notifyUsage(counter, name, "You should stop now!", text);
                break;
            case 100:
                notifyUsage(counter, name, "RIP Real Life, GG EZ", text);
                break;
            default:
                return;
        }
    }

    //TODO: move this to NotificationService class or something like that.
    private void notifyUsage(int counter, String name, String title, String text) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_noti)
                .setContentTitle(title)
                .setContentText(text);

        int mNotificationId = 001;

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
