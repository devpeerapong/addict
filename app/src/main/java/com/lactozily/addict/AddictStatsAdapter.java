package com.lactozily.addict;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.lactozily.addict.model.ProductObject;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class AddictStatsAdapter extends RealmBaseAdapter<ProductObject> implements ListAdapter {
    private int mTabPosition;
    protected PackageManager mPackageManager;
    protected Realm mRealm;

    protected static class MyViewHolder {
        TextView product_name_txt;
        TextView product_time_txt;
        TextView product_counter_txt;
        ImageView product_ic;
    }

    public AddictStatsAdapter(Context context,
                              RealmResults<ProductObject> realmResults,
                              boolean automaticUpdate, int tabPosition, Realm realm) {
        super(context, realmResults, automaticUpdate);
        mTabPosition = tabPosition;
        mPackageManager = context.getPackageManager();
        mRealm = realm;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MyViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.addict_stat_listview,
                    parent, false);
            viewHolder = new MyViewHolder();
            viewHolder.product_name_txt = (TextView) convertView.findViewById(R.id.product_name_txt);
            viewHolder.product_time_txt = (TextView) convertView.findViewById(R.id.product_time_txt);
            viewHolder.product_counter_txt = (TextView) convertView.findViewById(R.id.product_counter_txt);
            viewHolder.product_ic = (ImageView) convertView.findViewById(R.id.ic_product);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }

        final ProductObject item = realmResults.get(position);
        viewHolder.product_name_txt.setText(item.getProductName());

        PackageIconTask mPackageIconTask = (PackageIconTask)new PackageIconTask(new AsyncResponse() {
            @Override
            public void processFinish(Drawable output) {
                if (output != null) {
                    viewHolder.product_ic.setImageDrawable(output);
                }
            }
        }).execute(item.getPackageName());

        int size = 0;

        switch (mTabPosition) {
            case 0:
                Calendar cToday = Calendar.getInstance();
                cToday.set(Calendar.HOUR_OF_DAY, 0);
                cToday.set(Calendar.MINUTE, 0);
                cToday.set(Calendar.SECOND, 0);

                Calendar cTmr = Calendar.getInstance();
                cTmr.set(Calendar.HOUR_OF_DAY, 23);
                cTmr.set(Calendar.MINUTE, 59);
                cTmr.set(Calendar.SECOND, 59);
                size = item.getHistories().where().between("time", cToday.getTime(), cTmr.getTime()).findAll().size();
                break;
            case 1:
                Calendar cFirstDate = Calendar.getInstance();
                cFirstDate.set(Calendar.DAY_OF_MONTH, 1);
                cFirstDate.set(Calendar.HOUR_OF_DAY, 0);
                cFirstDate.set(Calendar.MINUTE, 0);
                cFirstDate.set(Calendar.SECOND, 0);

                Calendar cLastDate = Calendar.getInstance();
                cLastDate.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
                cLastDate.set(Calendar.HOUR_OF_DAY, 23);
                cLastDate.set(Calendar.MINUTE, 59);
                cLastDate.set(Calendar.SECOND, 59);

                size = item.getHistories().where().between("time", cFirstDate.getTime(), cLastDate.getTime()).findAll().size();
                break;
            case 2:
                size = item.getHistories().size();
                break;
            default:
        }

        final PrettyTime lt = new PrettyTime();

        viewHolder.product_counter_txt.setText(String.valueOf(size));
        if (item.getHistories().size() != 0) {
            viewHolder.product_time_txt.setText(lt.format(item.getHistories().last().getTime()));
        }

        return convertView;
    }

    public interface AsyncResponse {
        void processFinish(Drawable output);
    }

    private class PackageIconTask extends AsyncTask<String, String, Drawable> {
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

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
