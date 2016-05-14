package com.lactozily.addict;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lactozily on 5/10/2016 AD.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    PackageManager mPackageManager;
    List<AddictApplicationInfo> mSearchResults;
    List<AddictApplicationInfo> mVisibleSearchResult;

    public SearchResultAdapter(List<AddictApplicationInfo> SearchResults, PackageManager packageManager) {
        mSearchResults = SearchResults;
        mPackageManager = packageManager;
        mVisibleSearchResult = new ArrayList<>();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.searchable_recycleview, parent, false);
        ViewHolder vh = new ViewHolder((LinearLayout) v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ViewHolder mHolder = holder;
        String packageName = mVisibleSearchResult.get(position).getPackageName();
        String productName = mVisibleSearchResult.get(position).getProductName();
        holder.product_name_txt.setText(productName);



        PackageIconTask mPackageIconTask = (PackageIconTask)new PackageIconTask(new AsyncResponse() {
            @Override
            public void processFinish(Drawable output) {
                if (output != null) {
                    mHolder.product_ic.setImageDrawable(output);
                }
            }
        }).execute(packageName);
    }

    @Override
    public int getItemCount() {
        return mVisibleSearchResult.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView product_name_txt;
        ImageView product_ic;

        public ViewHolder(LinearLayout container) {
            super(container);
            this.product_name_txt = (TextView) container.findViewById(R.id.product_name_txt);
            this.product_ic = (ImageView) container.findViewById(R.id.ic_product);
        }
    }

    public void flushFilter(){
        mVisibleSearchResult = new ArrayList<>();
        mVisibleSearchResult.addAll(mSearchResults);
        notifyDataSetChanged();
    }

    public void setFilter(String queryText) {
        mVisibleSearchResult = new ArrayList<>();
        for (AddictApplicationInfo appInfo: mSearchResults) {
            if (appInfo.getProductName().toLowerCase().startsWith(queryText))
                mVisibleSearchResult.add(appInfo);
        }
        notifyDataSetChanged();
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
