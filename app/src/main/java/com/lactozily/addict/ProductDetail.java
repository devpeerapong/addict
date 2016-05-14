package com.lactozily.addict;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.lactozily.addict.model.ProductObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;

public class ProductDetail extends AppCompatActivity {

    static Realm realm;
    static PackageManager mPackageManager;

    ImageView product_ic;
    TextView product_name;
    TextView counter_daily;
    TextView counter_monthly;
    TextView counter_alltime;
    List<Calendar> calendar_list;
    List<Integer> counter_list;
    ProductWeekHistoryAdapter historyAdapter;
    RecyclerView recyclerView;
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        initializeToolbar();
        initializeData();
    }

    void initializeData() {
        realm = Realm.getDefaultInstance();
        mPackageManager = getPackageManager();
        product_ic = (ImageView) findViewById(R.id.ic_product);
        product_name = (TextView) findViewById(R.id.product_name_txt);
        counter_alltime = (TextView) findViewById(R.id.counter_alltime_txt);
        counter_daily = (TextView) findViewById(R.id.counter_daily_txt);
        counter_monthly = (TextView) findViewById(R.id.conter_monthly_txt);
        recyclerView = (RecyclerView) findViewById(R.id.last_7_days);

        Bundle getExtras = getIntent().getExtras();
        String productName = getExtras.getString("product_name");
        packageName = getExtras.getString("package_name");

        product_name.setText(productName);
        PackageIconTask mPackageIconTask = (PackageIconTask) new PackageIconTask(new AsyncResponse() {
            @Override
            public void processFinish(Drawable output) {
                if (output != null) {
                    product_ic.setImageDrawable(output);
                }
            }
        }).execute(packageName);

        ProductObject product = realm.where(ProductObject.class).equalTo("packageName", packageName).findFirst();
        counter_daily.setText(String.valueOf(product.getCounterDaily()));
        counter_monthly.setText(String.valueOf(product.getCounterMonthly()));
        counter_alltime.setText(String.valueOf(product.getCounterAllTime()));

        calendar_list = new ArrayList<>();
        counter_list = new ArrayList<>();

        Calendar today = AddictUtility.getStartTimeOfDate();
        for(int i = 1; i < 8; i++) {
            Calendar lastWeek = AddictUtility.getTimeInLastWeek(i);
            int counter = product.getHistories().where().between("time", lastWeek.getTime(), today.getTime()).findAll().size();
            calendar_list.add(lastWeek);
            counter_list.add(counter);
            today = lastWeek;
        }
        initializeRecyclerView();
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        try {
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    void initializeRecyclerView() {
        assert recyclerView != null;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        historyAdapter = new ProductWeekHistoryAdapter(counter_list, calendar_list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(historyAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.product_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.remove_product_menu:
                ProductObject product = realm.where(ProductObject.class).equalTo("packageName", packageName).findFirst();
                String productName = product.getProductName();
                realm.beginTransaction();
                product.removeFromRealm();
                realm.commitTransaction();
                Intent intent = new Intent();
                intent.putExtra("product_name", productName);
                setResult(Activity.RESULT_OK, intent);
                AddictMonitorService.updateQueryNeed = true;
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface AsyncResponse {
        void processFinish(Drawable output);
    }

    private static class PackageIconTask extends AsyncTask<String, String, Drawable> {
        public AsyncResponse delegate = null;

        public PackageIconTask(AsyncResponse delegate){
            this.delegate = delegate;
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
}
