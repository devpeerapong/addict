package com.lactozily.addict;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.lactozily.addict.model.ProductObject;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by lactozily on 2/27/2016 AD.
 */
public class AddictStatsAdapter extends RealmBaseAdapter<ProductObject> implements ListAdapter {
    private int mTabPosition;
    private static class MyViewHolder {
        TextView product_name_txt;
        TextView product_time_txt;
        TextView product_counter_txt;
    }

    public AddictStatsAdapter(Context context,
                              RealmResults<ProductObject> realmResults,
                              boolean automaticUpdate, int tabPosition) {
        super(context, realmResults, automaticUpdate);
        mTabPosition = tabPosition;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.addict_stat_listview,
                    parent, false);
            viewHolder = new MyViewHolder();
            viewHolder.product_name_txt = (TextView) convertView.findViewById(R.id.product_name_txt);
            viewHolder.product_time_txt = (TextView) convertView.findViewById(R.id.product_time_txt);
            viewHolder.product_counter_txt = (TextView) convertView.findViewById(R.id.product_counter_txt);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }

        ProductObject item = realmResults.get(position);
        viewHolder.product_name_txt.setText(item.getProductName());

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

        PrettyTime lt = new PrettyTime();

        viewHolder.product_counter_txt.setText(String.valueOf(size));
        if (item.getHistories().size() != 0) {
            viewHolder.product_time_txt.setText(lt.format(item.getHistories().first().getTime()));
        }

        return convertView;
    }
}
