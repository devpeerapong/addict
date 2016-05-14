package com.lactozily.addict;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

/**
 * Created by lactozily on 5/15/2016 AD.
 */
public class GetPackageIconTask extends AsyncTask<String, String, Drawable> {
    private AddictUtility.AsyncResponse delegate = null;
    private final PackageManager mPackageManager;

    public GetPackageIconTask(AddictUtility.AsyncResponse mDelegate, PackageManager pakageManager){
        delegate = mDelegate;
        mPackageManager = pakageManager;
    }

    @Override
    protected Drawable doInBackground(String... params) {
        Drawable icon = null;
        try {
            icon = mPackageManager.getApplicationIcon(params[0]);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return icon;
    }

    @Override
    protected void onPostExecute(Drawable icon) {
        delegate.processFinish(icon);
    }
}