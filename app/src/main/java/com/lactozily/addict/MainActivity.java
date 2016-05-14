package com.lactozily.addict;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.lactozily.addict.model.ProductObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    JobManager mJobManager;
    static Realm realm;
    static RealmResults<ProductObject> query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();
        query = realm.where(ProductObject.class).findAll();

        initializeToolbar();
        initializeFragment();

        mJobManager = JobManager.instance();

        if (!AddictUtility.isMyServiceRunning(this, AddictMonitorService.class)) {
            Intent intent = new Intent(this, AddictMonitorService.class);
            startService(intent);
        }
    }

    private void initializeFragment() {
        AddictPagerAdapter addictPagerAdapter = new AddictPagerAdapter(getSupportFragmentManager());
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
        new JobRequest.Builder(AddictServiceChecker.TAG)
                .setPeriodic(60_000L)
                .setPersisted(true)
                .build()
                .schedule();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AddictUtility.ADD_PRODUCT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Toast.makeText(this, "Add " + data.getStringExtra("product_name") + " Complete", Toast.LENGTH_SHORT).show();
        }
    }

    static class AddictPagerAdapter extends FragmentStatePagerAdapter {

        public AddictPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return AddictFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return AddictFragment.TAB_NAME[position];
        }
    }

    public static class AddictFragment extends Fragment {
        private static final String[] TAB_NAME = {"DAILY", "MONTHLY", "ALL TIME"};
        private static final String TAB_POSITION = "tab_position";
        private RealmRecyclerView realmRecyclerView;
        private AddictRecycleViewAdapter addictRecycleViewAdapter;


        public AddictFragment() {
        }

        public static AddictFragment newInstance(int tabPosition) {
            AddictFragment fragment = new AddictFragment();
            Bundle args = new Bundle();
            args.putInt(TAB_POSITION, tabPosition);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            int tabPosition = args.getInt(TAB_POSITION);

            realm = Realm.getDefaultInstance();

            View rootView = inflater.inflate(R.layout.addict_stats, container, false);
            realmRecyclerView = (RealmRecyclerView)rootView.findViewById(R.id.addict_stats_recycle_view);
            addictRecycleViewAdapter = new AddictRecycleViewAdapter(getContext(), query, false, true, tabPosition);
            realmRecyclerView.setAdapter(addictRecycleViewAdapter);

            TextView date_txt = (TextView)rootView.findViewById(R.id.date_txt);

            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy");
            String dt = "";

            switch (tabPosition) {
                case 0:
                    dt = sdf.format(Calendar.getInstance().getTime());
                    break;
                case 1:
                    sdf = new SimpleDateFormat("MMMM yyyy");
                    dt = "1 - " + Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) + " " + sdf.format(Calendar.getInstance().getTime()) ;
                    break;
                case 2:
                    dt = "All time";
                    break;
                default:
                    dt = sdf.format(Calendar.getInstance().getTime());
            }

            date_txt.setText(dt);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }

        @Override
        public void onResume() {
            super.onResume();
            Bundle args = getArguments();
            int tabPosition = args.getInt(TAB_POSITION);
            addictRecycleViewAdapter = new AddictRecycleViewAdapter(getContext(), query, false, true, tabPosition);
            realmRecyclerView.setAdapter(addictRecycleViewAdapter);
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            realm.close();
        }
    }
}
