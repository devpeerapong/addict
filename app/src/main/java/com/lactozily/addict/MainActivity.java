package com.lactozily.addict;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.lactozily.addict.model.ProductObject;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    private JobManager mJobManager;
    private static Realm realm;
    private static RealmResults<ProductObject> query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        realm = Realm.getDefaultInstance();
        query = realm.where(ProductObject.class).findAll();
        Context mContext = this;

        initializeToolbar();
        initializeFragment();

        mJobManager = JobManager.instance();

        if (!AddictUtility.isMyServiceRunning(this, AddictMonitorService.class)) {
            Intent intent = new Intent(this, AddictMonitorService.class);
            startService(intent);
        }
    }

    private void initializeFragment() {
        ProductListPagerAdapter addictPagerAdapter = new ProductListPagerAdapter(getSupportFragmentManager(), realm, query);
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        assert viewPager != null;
        viewPager.setAdapter(addictPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, SearchableActivity.class);
                startActivityForResult(intent, AddictUtility.ADD_PRODUCT_REQUEST_CODE);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mJobManager.cancelAll();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!AddictUtility.isMyServiceRunning(this, AddictMonitorService.class)) {
            Intent intent = new Intent(this, AddictMonitorService.class);
            startService(intent);
        }
        new JobRequest.Builder(AddictServiceChecker.TAG)
                .setPeriodic(60_000L)
                .setPersisted(true)
                .build()
                .schedule();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        Log.i("Result", String.valueOf(requestCode));
        Log.i("Result", String.valueOf(resultCode));
        Log.i("Result", data.toString());

        if (requestCode == AddictUtility.ADD_PRODUCT_REQUEST_CODE) {
            Toast.makeText(this, "Add " + data.getStringExtra("product_name") + " Complete", Toast.LENGTH_SHORT).show();
        } else if (requestCode == AddictUtility.REMOVE_PRODUCT_REQUEST_CODE) {
            Toast.makeText(this, "Remove " + data.getStringExtra("product_name") + " Complete", Toast.LENGTH_SHORT).show();
        }
    }
}
