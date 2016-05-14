package com.lactozily.addict;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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

    private final List<AddictApplicationInfo> mSearchResults;
    private static PackageManager mPackageManager;
    public List<AddictApplicationInfo> mVisibleSearchResult;
    private final OnClickListener mListener;

    public interface OnClickListener {
        void OnItemClick(int position);
    }

    public SearchResultAdapter(List<AddictApplicationInfo> SearchResults, PackageManager packageManager, OnClickListener listener) {
        mSearchResults = SearchResults;
        mPackageManager = packageManager;
        mVisibleSearchResult = new ArrayList<>();
        mListener = listener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.searchable_recycleview, parent, false);
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        String packageName = mVisibleSearchResult.get(position).getPackageName();
        String productName = mVisibleSearchResult.get(position).getProductName();
        holder.bind(packageName, productName);
    }

    @Override
    public int getItemCount() {
        return mVisibleSearchResult.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView product_name_txt;
        final ImageView product_ic;

        public ViewHolder(LinearLayout container) {
            super(container);
            product_name_txt = (TextView) container.findViewById(R.id.product_name_txt);
            product_ic = (ImageView) container.findViewById(R.id.ic_product);
        }

        public void bind(String packageName, String productName) {
            product_name_txt.setText(productName);
            new GetPackageIconTask(new AddictUtility.AsyncResponse() {
                @Override
                public void processFinish(Drawable output) {
                    if (output != null) {
                        product_ic.setImageDrawable(output);
                    }
                }
            }, mPackageManager).execute(packageName);
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
            if (appInfo.getProductName().toLowerCase().startsWith(queryText.toLowerCase()))
                mVisibleSearchResult.add(appInfo);
        }
        notifyDataSetChanged();
    }
}
