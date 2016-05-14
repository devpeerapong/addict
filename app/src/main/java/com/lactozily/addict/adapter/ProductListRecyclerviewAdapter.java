package com.lactozily.addict.adapter;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lactozily.addict.AddictUtility;
import com.lactozily.addict.GetPackageIconTask;
import com.lactozily.addict.R;
import com.lactozily.addict.model.ProductObject;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

/**
 * Created by lactozily on 5/15/2016 AD.
 */
public class ProductListRecyclerviewAdapter extends RecyclerView.Adapter<ProductListRecyclerviewAdapter.ViewHolder> {
    private static PackageManager mPackageManager;
    private final List<ProductObject> mProductList;
    private final AddictUtility.OnClickListener mListener;
    private final int mTabPosition;

    public ProductListRecyclerviewAdapter(PackageManager packageManager, int tab, AddictUtility.OnClickListener listener, List<ProductObject> productList) {
        mPackageManager = packageManager;
        mListener = listener;
        mTabPosition = tab;
        mProductList = productList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.addict_stat_listview, parent, false);
        final ViewHolder vh = new ViewHolder((LinearLayout) v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int _position = position;
        ProductObject productObject = mProductList.get(position);
        if (!productObject.isValid()) return;

        String productName = productObject.getProductName();
        String packageName = productObject.getPackageName();
        int counter = 0;
        String lastUsed = "no usage";
        if (productObject.getHistories() != null) {
            counter = getCounterByTabPosition(productObject);
        }


        if (productObject.getHistories().last() != null) {
            PrettyTime prettyTime = new PrettyTime();
            lastUsed = prettyTime.format(productObject.getHistories().last().getTime());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnItemClick(_position);
            }
        });

        holder.bind(packageName, productName, counter, lastUsed);

    }

    @Override
    public int getItemCount() {
        return mProductList.size();
    }

    private int getCounterByTabPosition(ProductObject productObject) {
        switch (mTabPosition) {
            case 0:
                return productObject.getCounterDaily();
            case 1:
                return productObject.getCounterMonthly();
            default:
                return productObject.getCounterAllTime();
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView product_name_txt;
        final TextView product_time_txt;
        final TextView product_counter_txt;
        final ImageView product_ic;

        public ViewHolder(LinearLayout container) {
            super(container);
            product_name_txt = (TextView) container.findViewById(R.id.product_name_txt);
            product_time_txt = (TextView) container.findViewById(R.id.product_time_txt);
            product_counter_txt = (TextView) container.findViewById(R.id.product_counter_txt);
            product_ic = (ImageView) container.findViewById(R.id.ic_product);
        }

        public void bind(String packageName, String productName, int counter, String last_used) {
            product_name_txt.setText(productName);
            product_counter_txt.setText(String.valueOf(counter));
            product_time_txt.setText(last_used);
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
}
