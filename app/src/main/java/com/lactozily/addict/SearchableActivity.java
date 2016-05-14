package com.lactozily.addict;

import android.app.SearchManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchableActivity extends AppCompatActivity {
    PackageManager mPackageManager;
    List<ApplicationInfo> installedApplications;
    List<AddictApplicationInfo> mApplicationInfo;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    SearchResultAdapter searchResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        mPackageManager = getPackageManager();
        installedApplications = new ArrayList<>();
        mApplicationInfo = new ArrayList<>();
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        initializeToolBar();
        initializeRecyclerView();

        new GetAllInstalledApplicationTask().execute();
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

    void initializeToolBar() {
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

    void initializeRecyclerView() {
        recyclerView = (RecyclerView)findViewById(R.id.search_results);
        assert recyclerView != null;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new SearchResultAdapter(mApplicationInfo, mPackageManager));
    }

    void initializeSearchView(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                ((SearchResultAdapter) recyclerView.getAdapter()).flushFilter();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });

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
                Log.i("searchView", query);
                SearchableActivity.this.finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == "") {
                    ((SearchResultAdapter) recyclerView.getAdapter()).flushFilter();
                    return true;
                }

                ((SearchResultAdapter) recyclerView.getAdapter()).setFilter(newText);
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
                mApplicationInfo.add(new AddictApplicationInfo(appInfo.loadLabel(mPackageManager).toString(), appInfo.packageName));
            }
            progressBar.setVisibility(View.INVISIBLE);
            searchResultAdapter = new SearchResultAdapter(mApplicationInfo, mPackageManager);
            recyclerView.setAdapter(searchResultAdapter);
        }
    }
}
