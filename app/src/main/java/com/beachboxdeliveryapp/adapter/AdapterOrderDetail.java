package com.beachboxdeliveryapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.model.ModelHistoryOrder;
import com.beachboxdeliveryapp.model.ModelOrderDetail;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * This class used to display selected order.
 * Created by bitware on 12/7/17.
 */

public class AdapterOrderDetail extends ArrayAdapter<ModelOrderDetail> {

    ArrayList<ModelOrderDetail> arrOrderDetail = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterOrderDetail(Context context, int resource, ArrayList<ModelOrderDetail> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrOrderDetail = arrRestaurantList;
        this.context = context;

        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tv_productName, tv_qty;
    }

    @Override
    public int getCount() {
        return arrOrderDetail.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_orderdetail, null);
            holder = new ViewHolder();

            holder.tv_productName = (TextView) convertView.findViewById(R.id.tv_productName);
            holder.tv_qty = (TextView) convertView.findViewById(R.id.tv_productQty);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_productName.setText(arrOrderDetail.get(position).getProductName());
        holder.tv_qty.setText("×"+" "+arrOrderDetail.get(position).getProductQty());

        return convertView;
    }
}
