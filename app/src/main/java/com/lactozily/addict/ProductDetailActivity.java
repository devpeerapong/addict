package com.lactozily.addict;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.lactozily.addict.adapter.ProductWeekHistoryAdapter;
import com.lactozily.addict.model.ProductObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;

public class ProductDetailActivity extends AppCompatActivity {

    private static Realm realm;

    private ImageView product_ic;
    private List<Calendar> calendar_list;
    private List<Integer> counter_list;
    private RecyclerView recyclerView;
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        initializeToolbar();
        initializeData();
    }

    private void initializeData() {
        realm = Realm.getDefaultInstance();
        product_ic = (ImageView) findViewById(R.id.ic_product);
        TextView product_name = (TextView) findViewById(R.id.product_name_txt);
        TextView counter_alltime = (TextView) findViewById(R.id.counter_alltime_txt);
        TextView counter_daily = (TextView) findViewById(R.id.counter_daily_txt);
        TextView counter_monthly = (TextView) findViewById(R.id.conter_monthly_txt);
        recyclerView = (RecyclerView) findViewById(R.id.last_7_days);

        Bundle getExtras = getIntent().getExtras();
        String productName = getExtras.getString("product_name");
        packageName = getExtras.getString("package_name");

        product_name.setText(productName);
        new GetPackageIconTask(new AddictUtility.AsyncResponse() {
            @Override
            public void processFinish(Drawable output) {
                if (output != null) {
                    product_ic.setImageDrawable(output);
                }
            }
        }, getPackageManager()).execute(packageName);

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

    private void initializeRecyclerView() {
        assert recyclerView != null;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ProductWeekHistoryAdapter historyAdapter = new ProductWeekHistoryAdapter(counter_list, calendar_list);
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
}
