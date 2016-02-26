package com.lactozily.addict;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class AddictMonitorService extends IntentService {
    public static final String TAG = AddictMonitorService.class.getSimpleName();

    public AddictMonitorService() { super("addict-monitor-service"); }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Start Monitoring");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (this) {
            while (true) {
                try {
                    wait(700);
                    Log.i(TAG, "Monitoring");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
