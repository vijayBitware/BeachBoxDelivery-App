package com.beachboxdeliveryapp.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.activity.ActivityDasshboard;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.fragment.FragmentUpdateAccount;
import com.beachboxdeliveryapp.fragment.OrderDetailsFragment;
import com.beachboxdeliveryapp.model.notificationResponse.Notification;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class used to disply list of notifications.
 * Created by bitwarepc on 11-Aug-17.
 */

public class AdapterNotification extends ArrayAdapter<Notification> {

    List<Notification> arrRestaurantList = new ArrayList<>();
    LayoutInflater inflater;
    Context context;
    ViewHolder holder;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent,isGPSEnabled = false,isResumeCalled = false;


    public AdapterNotification(Context context, int resource, List<Notification> arrRestaurantList) {
        super(context, resource, arrRestaurantList);
        this.arrRestaurantList = arrRestaurantList;
        this.context = context;
        cd = new ConnectionDetector(context);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static class ViewHolder {
        TextView tvNotiMsg,tvNotiDate,tvNotitypeOrder;
        ImageView iv_notiImg;
    }

    @Override
    public int getCount() {
        return arrRestaurantList.size();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_notification, null);
            holder = new ViewHolder();

            holder.iv_notiImg = (ImageView) convertView.findViewById(R.id.iv_notiImg);
            holder.tvNotiMsg= (TextView) convertView.findViewById(R.id.tvNotiMsg);
            holder.tvNotiDate= (TextView) convertView.findViewById(R.id.tvNotiDate);
            holder.tvNotitypeOrder = (TextView)  convertView.findViewById(R.id.tvNotitypeOrder);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String resImage = arrRestaurantList.get(position).getRestaurantImage();
        if (resImage.isEmpty() || resImage == null || resImage.equals("")){
            holder.iv_notiImg.setImageResource(R.drawable.blank_resturant);
        }else {
            Picasso.with(context)
                    .load(arrRestaurantList.get(position).getRestaurantImage())
                    .into(holder.iv_notiImg);
        }



        holder.tvNotiMsg.setText(arrRestaurantList.get(position).getNotification().toString().trim());
        holder.tvNotiDate.setText(getFormatedDateTime(arrRestaurantList.get(position).getNotificationDate()));
        String orderType = arrRestaurantList.get(position).getOrderType();
        holder.tvNotitypeOrder.setText(orderType.substring(0, 1).toUpperCase() + orderType.substring(1));



        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled){



                        editor.putString("orderStatus",arrRestaurantList.get(position).getOrderStatus());
                        editor.putString("order_id",arrRestaurantList.get(position).getOrderId().toString());
                        editor.putString("navigation","Notification");
                        editor.commit();
                        Config.isFROM = "Notification";
                        ((ActivityDasshboard)context).replaceFragment(new OrderDetailsFragment());
                       // replaceNewFragment(new OrderDetailsFragment());

                    }else{
                        showSettingsAlert();
                    }
                }else{
                    Toast.makeText(context, context.getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                }
            }
        });


        return convertView;
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

        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
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


    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps_enabled && network_enabled) {
                resVal = true;
            } else {
                resVal = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resVal;
    }
    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isResumeCalled = true;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}



