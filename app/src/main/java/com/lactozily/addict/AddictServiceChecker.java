package com.lactozily.addict;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
class AddictServiceChecker extends Job {
    public static final String TAG = "addict-service-checker";

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        Log.i(TAG, "Check Service");
        // run your job
        Context mContext = this.getContext();
        if (!AddictUtility.isMyServiceRunning(mContext, AddictMonitorService.class)) {
            Log.e(TAG, "Service is not running");
            Intent intent = new Intent(mContext, AddictMonitorService.class);
            this.getContext().startService(intent);
        }

        Log.i(TAG, "Service is running");
        return Result.SUCCESS;
    }
}
