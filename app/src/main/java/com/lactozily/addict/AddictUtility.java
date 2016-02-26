package com.lactozily.addict;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class AddictUtility {
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
}
