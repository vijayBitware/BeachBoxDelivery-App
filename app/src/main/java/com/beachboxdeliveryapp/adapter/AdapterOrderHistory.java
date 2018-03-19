package com.beachboxdeliveryapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.activity.ActivityDasshboard;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.fragment.OrderDetailsFragment;
import com.beachboxdeliveryapp.model.ModelHistoryOrder;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class used to display list of order history.
 * Created by bitware on 23/3/17.
 */

public class AdapterOrderHistory extends ArrayAdapter<ModelHistoryOrder> {

    ArrayList<ModelHistoryOrder> arrRestaurantList = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public AdapterOrderHistory(Context context, int resource, ArrayList<ModelHistoryOrder> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;

        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tv_restaurantName, tv_dateTime, tv_restaurantRating,tv_price,tv_createdAt;
        ImageView iv_restaurantImage,iv_rate1,iv_rate2,iv_rate3,iv_rate4,iv_rate5;
    }

    @Override
    public int getCount() {
        return arrRestaurantList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_history_orders, null);
            holder = new ViewHolder();

            holder.tv_restaurantName = (TextView) convertView.findViewById(R.id.tv_restaurantName);
            holder.tv_dateTime = (TextView) convertView.findViewById(R.id.tv_dateTime);
            holder.tv_restaurantRating = (TextView) convertView.findViewById(R.id.tv_rating);
            holder.iv_restaurantImage = (ImageView) convertView.findViewById(R.id.iv_restaurantImage);
            holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
            holder.tv_createdAt= (TextView) convertView.findViewById(R.id.tv_createdAt);
            holder.iv_rate1 = (ImageView) convertView.findViewById(R.id.iv_rate1);
            holder.iv_rate2 = (ImageView) convertView.findViewById(R.id.iv_rate2);
            holder.iv_rate3 = (ImageView) convertView.findViewById(R.id.iv_rate3);
            holder.iv_rate4 = (ImageView) convertView.findViewById(R.id.iv_rate4);
            holder.iv_rate5 = (ImageView) convertView.findViewById(R.id.iv_rate5);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_restaurantName.setText(arrRestaurantList.get(position).getResName());
        holder.tv_createdAt.setText(getFormatedDateTime(arrRestaurantList.get(position).getOrderCreatedAt()));

        holder.tv_price.setText("$"+arrRestaurantList.get(position).getResPrice());
        System.out.println(">>> Order id "+arrRestaurantList.get(position).getOrderId());

        String resImage = arrRestaurantList.get(position).getResImage();
        if (resImage.isEmpty() || resImage == null || resImage.equals("")){
            holder.iv_restaurantImage.setImageResource(R.drawable.blank_resturant);
        }else {
            Picasso.with(context)
                    .load(resImage)
                    .into(holder.iv_restaurantImage);
        }


        switch (arrRestaurantList.get(position).getResRating()){
            case "1":
                holder.iv_rate1.setImageResource(R.drawable.rating);
                holder.iv_rate2.setImageResource(R.drawable.gray_star);
                holder.iv_rate3.setImageResource(R.drawable.gray_star);
                holder.iv_rate4.setImageResource(R.drawable.gray_star);
                holder.iv_rate5.setImageResource(R.drawable.gray_star);
                break;
            case "2":
                holder.iv_rate1.setImageResource(R.drawable.rating);
                holder.iv_rate2.setImageResource(R.drawable.rating);
                holder.iv_rate3.setImageResource(R.drawable.gray_star);
                holder.iv_rate4.setImageResource(R.drawable.gray_star);
                holder.iv_rate5.setImageResource(R.drawable.gray_star);
                break;
            case "3":
                holder.iv_rate1.setImageResource(R.drawable.rating);
                holder.iv_rate2.setImageResource(R.drawable.rating);
                holder.iv_rate3.setImageResource(R.drawable.rating);
                holder.iv_rate4.setImageResource(R.drawable.gray_star);
                holder.iv_rate5.setImageResource(R.drawable.gray_star);
                break;
            case "4":
                holder.iv_rate1.setImageResource(R.drawable.rating);
                holder.iv_rate2.setImageResource(R.drawable.rating);
                holder.iv_rate3.setImageResource(R.drawable.rating);
                holder.iv_rate4.setImageResource(R.drawable.rating);
                holder.iv_rate5.setImageResource(R.drawable.gray_star);
                break;
            case "5":
                holder.iv_rate1.setImageResource(R.drawable.rating);
                holder.iv_rate2.setImageResource(R.drawable.rating);
                holder.iv_rate3.setImageResource(R.drawable.rating);
                holder.iv_rate4.setImageResource(R.drawable.rating);
                holder.iv_rate5.setImageResource(R.drawable.rating);
                break;
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("order_id",arrRestaurantList.get(position).getOrderId());
                editor.putString("navigation","fromOrderHistory");
                editor.putString("status",arrRestaurantList.get(position).getOrderStatus());
                editor.commit();
                Config.isFROM = "Orders";

                ((ActivityDasshboard)context).replaceFragment(new OrderDetailsFragment());

                //context.startActivity(new Intent(context, ActivityOrderDetails.class));
            }
        });
        return convertView;
    }

    public void replaceNewFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = ((ActivityDasshboard) getContext()).getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.commit();
        }
    }
    public String getFormatedDateTime(String orderedDate) {
        String myMonth = "";
        String[] arrTest = orderedDate.split("\\s+");
        String mDate = arrTest[0];
        String mTime = arrTest[1];

        String[] actDate = mDate.split("-");
        String dateYear = actDate[0];
        String datemonth = actDate[1];
        String datday = actDate[2];


        switch (datemonth){
            case "01":
                myMonth = "Jan";
                break;
            case "02":
                myMonth = "Feb";
                break;
            case "03":
                myMonth = "Mar";
                break;
            case "04":
                myMonth = "Apr";
                break;
            case "05":
                myMonth = "May";
                break;
            case "06":
                myMonth = "Jun";
                break;
            case "07":
                myMonth = "Jul";
                break;
            case "08":
                myMonth = "Aug";
                break;
            case "09":
                myMonth = "Sept";
                break;
            case "10":
                myMonth = "Oct";
                break;
            case "11":
                myMonth = "Nov";
                break;

            case "12":
                myMonth = "Dec";
                break;
        }
        String newVal = myMonth+" "+datday;

        String[] actTime = mTime.split(":");
        String time1 = actTime[0];
        String time2 = actTime[1];
        String time3 = actTime[2];
        String strAMPMVal = "";
        if(Integer.parseInt(time1) < 12){
            strAMPMVal = "AM";
        }else{
            strAMPMVal = "PM";
        }
        String newtime = time1+":"+time2;//+" "+strAMPMVal;


        String start_dt = newtime;
        String formatedTime = null;
        DateFormat parser = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = (Date) parser.parse(start_dt);
            DateFormat formatter = new SimpleDateFormat("hh:mm");
            System.out.println("******dateeeeeeeeeee******"+formatter.format(date));
            formatedTime = formatter.format(date)+" "+strAMPMVal;
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return newVal+" "+formatedTime;
    }

}
