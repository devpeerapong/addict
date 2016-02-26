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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    JobManager mJobManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        AddictPagerAdapter addictPagerAdapter = new AddictPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(addictPagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab);
        tabLayout.setupWithViewPager(viewPager);

        mJobManager = JobManager.instance();

        Log.i(TAG, "Create Activity");

        if (!AddictUtility.isMyServiceRunning(this, AddictMonitorService.class)) {
            Intent intent = new Intent(this, AddictMonitorService.class);
            startService(intent);
            Log.i(TAG, "Service not running");
        }

        Log.i(TAG, "Service running");
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
        private static final String[] TAB_NAME = {"DAILY", "MONTHLY", "ALLTIME"};
        private static final String TAB_POSITION = "tab_position";

        public AddictFragment() {
        }

        public static AddictFragment newInstance(int tabPosition) {
            AddictFragment fragment = new AddictFragment();
            Bundle args = new Bundle();
            args.putInt(TAB_POSITION, tabPosition);
            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle args = getArguments();
            int tabPosition = args.getInt(TAB_POSITION);
            TextView tv = new TextView(getActivity());
            tv.setGravity(Gravity.CENTER);
            tv.setText("Text in Tab #" + tabPosition);
            return tv;
        }
    }
}
