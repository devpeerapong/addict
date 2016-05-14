package com.lactozily.addict;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by lactozily on 5/14/2016 AD.
 */
public class ProductWeekHistoryAdapter extends RecyclerView.Adapter<ProductWeekHistoryAdapter.ViewHolder> {

    static List<Calendar> mCalendars;
    static List<Integer> mCounters;

    public ProductWeekHistoryAdapter(List<Integer> counter, List<Calendar> calendars) {
        mCounters = counter;
        mCalendars = calendars;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_detail_last_7_days, parent, false);
        final ViewHolder vh = new ViewHolder((LinearLayout) v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy");
        String date = sdf.format(mCalendars.get(position).getTime());
        Integer counter = mCounters.get(position);
        holder.bind(date, counter, position);
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView counter_txt;
        TextView date_txt;
        ImageView condition_ic;

        public ViewHolder(LinearLayout container) {
            super(container);
            date_txt = (TextView) container.findViewById(R.id.date_txt);
            counter_txt = (TextView) container.findViewById(R.id.counter_txt);
            condition_ic = (ImageView) container.findViewById(R.id.condition_ic);
        }

        public void bind(String date, Integer counter, int position) {
            counter_txt.setText(counter.toString());
            date_txt.setText(date);

            if (position == 6) {
                return;
            }

            int previousDay = mCounters.get(position + 1);
            if (counter > previousDay) {
                condition_ic.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
            } else if (counter < previousDay) {
                condition_ic.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
            } else {
                condition_ic.setImageResource(R.drawable.ic_remove_black_24dp);
            }

        }
    }
}
