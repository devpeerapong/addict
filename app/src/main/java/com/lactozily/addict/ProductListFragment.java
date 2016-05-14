package com.lactozily.addict;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lactozily.addict.adapter.ProductListRecyclerviewAdapter;
import com.lactozily.addict.model.ProductObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by lactozily on 5/15/2016 AD.
 */
public class ProductListFragment extends Fragment {
    private static final String TAB_POSITION = "tab_position";
    private static final String DATE_FORMAT = "date_format";
    private static Realm realm;
    private static RealmResults<ProductObject> query;
    private RecyclerView recyclerView;
    private List<ProductObject> productList;
    private int mTabPosition;

    public static ProductListFragment newInstance(Realm r, RealmResults<ProductObject> q, int tabPosition, String date) {
        realm = r;
        query = q;

        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        args.putString(DATE_FORMAT, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        mTabPosition = args.getInt(TAB_POSITION);
        View rootView = inflater.inflate(R.layout.addict_stats, container, false);
        TextView date_txt = (TextView)rootView.findViewById(R.id.date_txt);
        date_txt.setText(args.getString(DATE_FORMAT));
        initializeRecyclerView(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();
        query = realm.allObjects(ProductObject.class);
        productList = new ArrayList<>();

        for(int i = 0; i < query.size(); i++) {
            productList.add(query.get(i));
        }

        Collections.sort(productList, new MostUseComparator());


        ProductListRecyclerviewAdapter productListRecyclerviewAdapter = new ProductListRecyclerviewAdapter(getActivity().getPackageManager(),
                mTabPosition,
                new AddictUtility.OnClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        Intent intent = new Intent(getActivity().getBaseContext(), ProductDetailActivity.class);
                        String packageName = productList.get(position).getPackageName();
                        String productName = productList.get(position).getProductName();
                        intent.putExtra("package_name", packageName);
                        intent.putExtra("product_name", productName);

                        getActivity().startActivityForResult(intent, AddictUtility.REMOVE_PRODUCT_REQUEST_CODE);
                    }
                }, productList);
        recyclerView.setAdapter(productListRecyclerviewAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void initializeRecyclerView(View rootView) {
        productList = new ArrayList<>();
        recyclerView = (RecyclerView)rootView.findViewById(R.id.product_list);
        assert recyclerView != null;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ProductListRecyclerviewAdapter(getActivity().getPackageManager(), mTabPosition, null, productList));

    }

    private class MostUseComparator implements Comparator<ProductObject> {
        @Override
        public int compare(ProductObject plhs, ProductObject prhs) {
            int lhs;
            int rhs;
            switch (mTabPosition) {
                case 0:
                    lhs = plhs.getCounterDaily();
                    rhs = prhs.getCounterDaily();
                    break;
                case 1:
                    lhs = plhs.getCounterMonthly();
                    rhs = prhs.getCounterMonthly();
                    break;
                default:
                    lhs = plhs.getCounterAllTime();
                    rhs = prhs.getCounterAllTime();
                    break;
            }
            return (lhs > rhs) ? -1 : (lhs == rhs) ? 0 : 1;
        }
    }
}
