package com.lactozily.addict;

import android.app.Application;
import android.content.Intent;
import android.provider.Settings;

import com.evernote.android.job.JobManager;
import com.lactozily.addict.model.ProductObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class AddictApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!AddictUtility.usageAccessGranted(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        JobManager.create(this).addJobCreator(new AddictJobCreator());

        RealmConfiguration config = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .name("addict")
                .schemaVersion(0)
                .build();

        Realm.setDefaultConfiguration(config);

        Realm realm = Realm.getDefaultInstance();

        if(realm.where(ProductObject.class).findAll().size() == 0) {
            ProductObject fb = new ProductObject();
            fb.setProductName("Facebook");
            fb.setPackageName("com.facebook.katana");

            ProductObject li = new ProductObject();
            li.setProductName("Line");
            li.setPackageName("jp.naver.line.android");

            ProductObject ig = new ProductObject();
            ig.setProductName("Instagram");
            ig.setPackageName("com.instagram.android");

            ProductObject sn = new ProductObject();
            sn.setProductName("Snapchat");
            sn.setPackageName("com.snapchat.android");

            ProductObject tw = new ProductObject();
            tw.setProductName("Twitter");
            tw.setPackageName("com.twitter.android");

            ProductObject bw = new ProductObject();
            bw.setProductName("Chrome");
            bw.setPackageName("com.android.browser");

            ProductObject ch = new ProductObject();
            bw.setProductName("Browser");
            bw.setPackageName("com.android.browser");

            realm.beginTransaction();
            realm.copyToRealm(fb);
            realm.copyToRealm(li);
            realm.copyToRealm(ig);
            realm.copyToRealm(sn);
            realm.copyToRealm(tw);
            realm.copyToRealm(bw);
            realm.commitTransaction();
        }
    }
}
