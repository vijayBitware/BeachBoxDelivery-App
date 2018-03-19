package com.beachboxdeliveryapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;

import com.beachboxdeliveryapp.activity.ActivityDasshboard;
import com.beachboxdeliveryapp.activity.CapturePhotoActivity;
import com.beachboxdeliveryapp.activity.SignInActivity;
import com.beachboxdeliveryapp.adapter.AdapterOrderHistory;
import com.beachboxdeliveryapp.adapter.AdapterUpcomingOrder;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.domain.GPSTracker;
import com.beachboxdeliveryapp.domain.LocationService;
import com.beachboxdeliveryapp.model.ModelHistoryOrder;
import com.beachboxdeliveryapp.model.ModelUpcomingOrder;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;


/**
 * This class for displaying list of upcoming orders and order history.
 * Created by bitware on 10/7/17.
 */

public class OrdersFragment extends Fragment {

    View view;
    ListView lv_upcomingOrders;
    TextView tv_upcoming,tv_history;
    AdapterUpcomingOrder adapterUpcomingOrder;
    AdapterOrderHistory adapterOrderHistory;
    ArrayList<ModelUpcomingOrder> arrUpcomingOrder;
    ArrayList<ModelHistoryOrder> arrHistoryOrder;
    ImageView iv_upcoming,iv_history;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    JSONArray upcomingorders;
    GPSTracker gpsTracker;
    double currentLat,currentLang;
    String  strLat = "",strLang = "";
    boolean isGPSEnabled = false;
    boolean isResumeCalled = false, uppcomingClicked = false,historyClicked = false;
    String session_userToken;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      if(view == null){
          view = inflater.inflate(R.layout.fragment_orders,container,false);
          init();
           session_userToken = sharedPreferences.getString("session_usertoken","");

          if (isInternetPresent) {
              isGPSEnabled = checkGPSEnabled();
              if(isGPSEnabled){
                  uppcomingClicked = true;
                  startUpdatingDeliveryLocation();   // Call Delivery Loctation service android
                  upcomingOrdersAPICalled(session_userToken);

              }else{
                  showSettingsAlert();
              }
          }else {
              Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
          }

          tv_history.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  iv_history.setVisibility(View.VISIBLE);
                  iv_upcoming.setVisibility(View.INVISIBLE);
                  String session_userToken = sharedPreferences.getString("session_usertoken","");
                  if(isInternetPresent){
                      new OrderHistory().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"from_date\":\"" + "" + "\",\"to_date\":\"" + "" + "\",\"status\":\"" + "" + "\",\"session_user_token\":\"" + session_userToken+ "\"}");
                  }
                  else {
                      Toast.makeText(getContext(),R.string.no_internet, Toast.LENGTH_SHORT).show();
                  }
              }
          });

          tv_upcoming.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  iv_upcoming.setVisibility(View.VISIBLE);
                  iv_history.setVisibility(View.INVISIBLE);
                  String session_userToken = sharedPreferences.getString("session_usertoken","");
                  if(isInternetPresent){
                      upcomingOrdersAPICalled(session_userToken);
                  }
                  else {
                      Toast.makeText(getContext(),R.string.no_internet, Toast.LENGTH_SHORT).show();
                  }
              }
          });

      }
        return view;
    }

    private void startUpdatingDeliveryLocation() {
        getActivity().startService(new Intent(getActivity(), LocationService.class));
    }

    private void upcomingOrdersAPICalled(String session_userToken) {
        new UpcomingOrders().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"session_user_token\":\"" + session_userToken+ "\"}");
    }

    private void init() {
        Log.e("Token#", FirebaseInstanceId.getInstance().getToken());
        sharedPreferences = getContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        iv_upcoming = (ImageView) view.findViewById(R.id.iv_upcoming);
        iv_history = (ImageView) view.findViewById(R.id.iv_history);
        tv_upcoming= (TextView) view.findViewById(R.id.tv_upcoming);
        tv_history= (TextView) view.findViewById(R.id.tv_history);
        lv_upcomingOrders = (ListView) view.findViewById(R.id.lv_orders);

        gpsTracker = new GPSTracker(getActivity());
        gpsTracker.getLocation();
        currentLat = gpsTracker.getLatitude();
        currentLang = gpsTracker.getLongitude();

        if(!String.valueOf(currentLat).isEmpty() &&  String.valueOf(currentLat).length() > 4){
           strLat = String.valueOf(currentLat);
            strLang = String.valueOf(currentLang);

            /*strLat = getFormatedLatLong(currentLat);
            strLang = getFormatedLatLong(currentLang);*/
        }

    }

    class UpcomingOrders extends AsyncTask<String, Void, String> {

        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getContext());
            p.setMessage("Please wait..");
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"upcomingorders")
                    .post(body)
                    .build();

            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>> Upcoming Orders result :" + s);
            p.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");

                    if(is_success==true){

                         upcomingorders = jsonObject.getJSONArray("upcomingorders");
                        arrUpcomingOrder=new ArrayList<>();
                        for (int i=0;i<upcomingorders.length();i++){
                            JSONObject orderObj = upcomingorders.getJSONObject(i);
                            ModelUpcomingOrder modelUpcomingOrder = new ModelUpcomingOrder();
                            modelUpcomingOrder.setRs_name(orderObj.getString("rs_name"));
                            modelUpcomingOrder.setRs_description(orderObj.getString("rs_description"));
                            modelUpcomingOrder.setRating(orderObj.getString("rating"));
                            modelUpcomingOrder.setRestaurant_image(orderObj.getString("restaurant_image"));
                            modelUpcomingOrder.setId(orderObj.getString("id"));
                            modelUpcomingOrder.setStatus(orderObj.getString("status"));
                            modelUpcomingOrder.setSub_total(orderObj.getString("sub_total"));
                            modelUpcomingOrder.setCreated_at(orderObj.getString("created_at"));
                            modelUpcomingOrder.setTotal_amount(orderObj.getString("total_amount"));
                            arrUpcomingOrder.add(modelUpcomingOrder);
                        }

                        if(upcomingorders.length() > 0){
                            adapterUpcomingOrder = new AdapterUpcomingOrder(getContext(),R.layout.row_upcoming_orders,arrUpcomingOrder);
                            lv_upcomingOrders.setAdapter(adapterUpcomingOrder);
                        }else{
                            Toast.makeText(getActivity(), "No upcoming orders found", Toast.LENGTH_SHORT).show();
                            adapterUpcomingOrder = new AdapterUpcomingOrder(getContext(),R.layout.row_upcoming_orders,arrUpcomingOrder);
                            lv_upcomingOrders.setAdapter(adapterUpcomingOrder);

                        }


                        /*if(sharedPreferences.getString("firstTimeLat","").equalsIgnoreCase(String.valueOf(strLat))){
                            if(upcomingorders.length() > 0){
                                adapterUpcomingOrder = new AdapterUpcomingOrder(getContext(),R.layout.row_upcoming_orders,arrUpcomingOrder);
                                lv_upcomingOrders.setAdapter(adapterUpcomingOrder);
                            }else{
                                Toast.makeText(getActivity(), "No upcoming orders found", Toast.LENGTH_SHORT).show();
                                adapterUpcomingOrder = new AdapterUpcomingOrder(getContext(),R.layout.row_upcoming_orders,arrUpcomingOrder);
                                lv_upcomingOrders.setAdapter(adapterUpcomingOrder);

                            }
                        }else{
                            inserUserLocationAPI();
                        }*/
                    }
                    else {
                        p.dismiss();
                        arrUpcomingOrder = new ArrayList<>();
                        adapterUpcomingOrder = new AdapterUpcomingOrder(getContext(),R.layout.row_upcoming_orders,arrUpcomingOrder);
                        lv_upcomingOrders.setAdapter(adapterUpcomingOrder);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                p.dismiss();
                Toast.makeText(getContext(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class OrderHistory extends AsyncTask<String, Void, String> {

        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getContext());
            p.setMessage("Please wait..");
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            Log.e("request", params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"orderhistory")
                    .post(body)
                    .build();

            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>>Orders history result :" + s);
            p.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");

                    if(is_success==true){

                        JSONArray orderdetails = jsonObject.getJSONArray("orderdetails");
                        arrHistoryOrder = new ArrayList<>();
                        for (int i=0;i<orderdetails.length();i++){
                            JSONObject orderObj = orderdetails.getJSONObject(i);
                            ModelHistoryOrder modelHistoryOrder = new ModelHistoryOrder();

                            modelHistoryOrder.setResName(orderObj.getString("rs_name"));
                            modelHistoryOrder.setResRating(orderObj.getString("rating"));
                            modelHistoryOrder.setOrderCreatedAt(orderObj.getString("ordered_date"));
                            modelHistoryOrder.setResImage(orderObj.getString("rs_pic"));
                            modelHistoryOrder.setOrderStatus(orderObj.getString("status"));
                            modelHistoryOrder.setResPrice(orderObj.getString("price"));
                            modelHistoryOrder.setOrderId(orderObj.getString("order_id"));
                            arrHistoryOrder.add(modelHistoryOrder);
                        }

                        if(orderdetails.length() > 0){
                            adapterOrderHistory = new AdapterOrderHistory(getContext(),R.layout.row_history_orders,arrHistoryOrder);
                            lv_upcomingOrders.setAdapter(adapterOrderHistory);
                        }else{
                            Toast.makeText(getActivity(), "No order history found", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else {
                        p.dismiss();
                        arrHistoryOrder = new ArrayList<>();
                        adapterOrderHistory = new AdapterOrderHistory(getContext(),R.layout.row_history_orders,arrHistoryOrder);
                        lv_upcomingOrders.setAdapter(adapterOrderHistory);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                p.dismiss();
                Toast.makeText(getContext(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void inserUserLocationAPI() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());

        /*String  firstTimeLat = getFormatedLatLong(gpsTracker.getLatitude());
        String  firstTimeLang = getFormatedLatLong(gpsTracker.getLongitude());*/

        String  firstTimeLat = String.valueOf(gpsTracker.getLatitude());
        String  firstTimeLang = String.valueOf(gpsTracker.getLongitude());

        System.out.println(">>> User first time Lat Long :"+firstTimeLat+"--"+firstTimeLang);
        JSONObject loationObj = new JSONObject();
        try {
            editor.putString("firstTimeLat",firstTimeLat);
            editor.putString("firstTimeLang",firstTimeLang);
            editor.commit();

            loationObj.put("latitude",firstTimeLat);
            loationObj.put("longitude",firstTimeLang);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray locationArray = new JSONArray();
        locationArray.put(loationObj);
        if (isInternetPresent){
            new updateLocationAPI().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"session_user_token\":\"" + sharedPreferences.getString("session_usertoken","") + "\",\"location_details\":" + locationArray + " }");
        }else {
            Toast.makeText(getActivity(),R.string.noNetworkMsg,Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormatedLatLong(double currentLat) {
        String strLat = "";
        String[] arr=String.valueOf(currentLat).split("\\.");
        Long[] longArr=new Long[2];

        longArr[0]=Long.parseLong(arr[0]); // 1
        longArr[1]=Long.parseLong(arr[1]); //

        String mainDigits = String.valueOf(longArr[0]);
        String strLenght = String.valueOf(longArr[1]);
        String first4char = strLenght.substring(0,4);
        String strFinal = mainDigits+"."+first4char;
        System.out.println(">>>> return val --- :"+strFinal);
        return strFinal;
    }


    class updateLocationAPI extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setMessage("Loading..");
            p.setCancelable(false);
            p.setCanceledOnTouchOutside(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Location Update params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"updateuserlocation")
                    .post(body)
                    .build();
            try
            {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            p.dismiss();
            System.out.println(">>> LocationUpdate Result on orders:" + s);

                      if(upcomingorders.length() > 0){
                            adapterUpcomingOrder = new AdapterUpcomingOrder(getContext(),R.layout.row_upcoming_orders,arrUpcomingOrder);
                            lv_upcomingOrders.setAdapter(adapterUpcomingOrder);
                        }else{
                            Toast.makeText(getActivity(), "No upcoming orders found", Toast.LENGTH_SHORT).show();
                            adapterUpcomingOrder = new AdapterUpcomingOrder(getContext(),R.layout.row_upcoming_orders,arrUpcomingOrder);
                            lv_upcomingOrders.setAdapter(adapterUpcomingOrder);

                        }
        }
    }

    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getActivity());
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("GPS is not enabled. Please enable the GPS setting.");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                isResumeCalled = true;
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
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

    @Override
    public void onResume() {
        super.onResume();
        if(isResumeCalled){
            upcomingOrdersAPICalled(session_userToken);

        }
    }
}
