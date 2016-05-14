package com.lactozily.addict;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import java.util.Calendar;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
class AddictUtility {
    public static final int ADD_PRODUCT_REQUEST_CODE = 522;
    public static final int REMOVE_PRODUCT_REQUEST_CODE = 10;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean usageAccessGranted(Context context) {
        AppOpsManager appOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isScreenLocked(Context context) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }

    public static Calendar getStartTimeOfDate() {
        Calendar cToday = Calendar.getInstance();
        cToday.set(Calendar.HOUR_OF_DAY, 0);
        cToday.set(Calendar.MINUTE, 0);
        cToday.set(Calendar.SECOND, 0);
        return cToday;
    }

    public static Calendar getTimeInLastWeek(int date) {
        Long lastWeekTimestamp = getStartTimeOfDate().getTimeInMillis() - (86400000 * date);
        Calendar cLastWeek = Calendar.getInstance();
        cLastWeek.setTimeInMillis(lastWeekTimestamp);
        return cLastWeek;
    }

    public static Calendar getEndTimeOfDate() {
        Calendar cTmr = Calendar.getInstance();
        cTmr.set(Calendar.HOUR_OF_DAY, 23);
        cTmr.set(Calendar.MINUTE, 59);
        cTmr.set(Calendar.SECOND, 59);
        return cTmr;
    }

    public static Calendar getStartTimeOfMonth() {
        Calendar cFirstDate = Calendar.getInstance();
        cFirstDate.set(Calendar.DAY_OF_MONTH, 1);
        cFirstDate.set(Calendar.HOUR_OF_DAY, 0);
        cFirstDate.set(Calendar.MINUTE, 0);
        cFirstDate.set(Calendar.SECOND, 0);

        return cFirstDate;
    }

    public static Calendar getEndTimeOfMonth() {
        Calendar cLastDate = Calendar.getInstance();
        cLastDate.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
        cLastDate.set(Calendar.HOUR_OF_DAY, 23);
        cLastDate.set(Calendar.MINUTE, 59);
        cLastDate.set(Calendar.SECOND, 59);

        return cLastDate;
    }

    public interface AsyncResponse {
        void processFinish(Drawable output);
    }

    public interface OnClickListener {
        void OnItemClick(int position);
    }
}
