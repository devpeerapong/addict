package com.lactozily.addict;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.lactozily.addict.adapter.AddProductRecyclerViewAdapter;
import com.lactozily.addict.model.AddictApplicationInfo;
import com.lactozily.addict.model.ProductObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;

public class AddProductActivity extends AppCompatActivity {
    private PackageManager mPackageManager;
    private List<ApplicationInfo> installedApplications;
    private List<AddictApplicationInfo> mApplicationInfo;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AddProductRecyclerViewAdapter addProductRecyclerViewAdapter;
    private Realm realm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        realm = Realm.getDefaultInstance();
        mPackageManager = getPackageManager();
        installedApplications = new ArrayList<>();
        mApplicationInfo = new ArrayList<>();
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        initializeToolBar();
        initializeRecyclerView();

        new GetAllInstalledApplicationTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchable_menu, menu);

        initializeSearchView(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeToolBar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_search);

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
        recyclerView = (RecyclerView)findViewById(R.id.search_results);
        assert recyclerView != null;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new AddProductRecyclerViewAdapter(mApplicationInfo, mPackageManager, new AddProductRecyclerViewAdapter.OnClickListener() {
            @Override
            public void OnItemClick(int position) {
                Log.i("ClickItem", addProductRecyclerViewAdapter.mVisibleSearchResult.get(position).getPackageName());
            }
        }));
    }

    private void initializeSearchView(Menu menu) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setQueryHint("Search");

        ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.rgb(244,244,244));
        ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(Color.rgb(222,222,222));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                AddProductActivity.this.setResult(Activity.RESULT_CANCELED);
                AddProductActivity.this.finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (Objects.equals(newText, "")) {
                    ((AddProductRecyclerViewAdapter) recyclerView.getAdapter()).flushFilter();
                    return true;
                }

                ((AddProductRecyclerViewAdapter) recyclerView.getAdapter()).setFilter(newText);
                return true;
            }
        });
    }

    private class GetAllInstalledApplicationTask extends AsyncTask<Void, Void, List<ApplicationInfo>> {

        @Override
        protected List<ApplicationInfo> doInBackground(Void... params) {
            return mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        }

        protected void onPostExecute(List<ApplicationInfo> result) {
            installedApplications = result;
            Collections.sort(installedApplications, new ApplicationInfo.DisplayNameComparator(mPackageManager));
            for (ApplicationInfo appInfo : installedApplications) {
                long size = realm.where(ProductObject.class).equalTo("packageName", appInfo.packageName).count();
                if(size != 0 || isSystemPackage(appInfo)) {
                    continue;
                }
                mApplicationInfo.add(new AddictApplicationInfo(appInfo.loadLabel(mPackageManager).toString(), appInfo.packageName));
            }
            progressBar.setVisibility(View.INVISIBLE);
            addProductRecyclerViewAdapter = new AddProductRecyclerViewAdapter(mApplicationInfo, mPackageManager, new AddProductRecyclerViewAdapter.OnClickListener() {
                @Override
                public void OnItemClick(int position) {
                    AddictApplicationInfo selectedApp = addProductRecyclerViewAdapter.mVisibleSearchResult.get(position);
                    ProductObject product = new ProductObject();
                    product.setProductName(selectedApp.getProductName());
                    product.setPackageName(selectedApp.getPackageName());
                    product.setCounterDaily(0);
                    product.setCounterMonthly(0);
                    product.setCounterAllTime(0);

                    realm.beginTransaction();
                    realm.copyToRealm(product);
                    realm.commitTransaction();
                    AddictMonitorService.updateQueryNeed = true;
                    Intent intent = new Intent();
                    intent.putExtra("product_name", selectedApp.getProductName());
                    AddProductActivity.this.setResult(Activity.RESULT_OK, intent);
                    AddProductActivity.this.finish();
                }
            });
            addProductRecyclerViewAdapter.flushFilter();
            recyclerView.setAdapter(addProductRecyclerViewAdapter);
        }
    }

    private boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }
}
