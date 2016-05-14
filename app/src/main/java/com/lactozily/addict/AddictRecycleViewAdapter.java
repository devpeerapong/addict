package com.lactozily.addict;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lactozily.addict.model.ProductObject;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;

import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by lactozily on 3/16/2016 AD.
 */
public class AddictRecycleViewAdapter extends RealmBasedRecyclerViewAdapter<ProductObject, AddictRecycleViewAdapter.ViewHolder> {
    int mTabPosition;
    protected PackageManager mPackageManager;
    OnClickListener mListener;

    public interface OnClickListener {
        void OnItemClick(int position);
    }

    public AddictRecycleViewAdapter(
            Context context,
            RealmResults<ProductObject> realmResults,
            boolean automaticUpdate,
            boolean animateIdType,
            int tabPosition,
            OnClickListener listener) {
        super(context, realmResults, automaticUpdate, animateIdType);
        mTabPosition = tabPosition;
        mPackageManager = context.getPackageManager();
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.addict_stat_listview, viewGroup, false);
        final ViewHolder vh = new ViewHolder((LinearLayout) v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnItemClick(vh.getLayoutPosition());
            }
        });
        return vh;
    }

    @Override
    public void onBindRealmViewHolder(ViewHolder viewHolder, int position) {
        final ProductObject item = realmResults.get(position);
        final ViewHolder mViewHolder = viewHolder;
        viewHolder.product_name_txt.setText(item.getProductName());

        PackageIconTask mPackageIconTask = (PackageIconTask)new PackageIconTask(new AsyncResponse() {
            @Override
            public void processFinish(Drawable output) {
                if (output != null) {
                    mViewHolder.product_ic.setImageDrawable(output);
                }
            }
        }).execute(item.getPackageName());

        int size = 0;

        switch (mTabPosition) {
            case 0:
                size = item.getCounterDaily();
                break;
            case 1:
                size = item.getCounterMonthly();
                break;
            case 2:
                size = item.getCounterAllTime();
                break;
            default:
        }

        final PrettyTime lt = new PrettyTime();

        viewHolder.product_counter_txt.setText(String.valueOf(size));
        if (item.getHistories().size() != 0) {
            viewHolder.product_time_txt.setText(lt.format(item.getHistories().last().getTime()));
        }
    }

    public class ViewHolder extends RealmViewHolder {
        TextView product_name_txt;
        TextView product_time_txt;
        TextView product_counter_txt;
        ImageView product_ic;

        public ViewHolder(LinearLayout container) {
            super(container);
            this.product_name_txt = (TextView) container.findViewById(R.id.product_name_txt);
            this.product_time_txt = (TextView) container.findViewById(R.id.product_time_txt);
            this.product_counter_txt = (TextView) container.findViewById(R.id.product_counter_txt);
            this.product_ic = (ImageView) container.findViewById(R.id.ic_product);
        }
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
}
