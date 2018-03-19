package com.beachboxdeliveryapp.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.activity.ActivityDasshboard;
import com.beachboxdeliveryapp.adapter.AdapterOrderDetail;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.domain.GPSTracker;
import com.beachboxdeliveryapp.model.ModelOrderDetail;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;


/**
 * This class for displayig order details.
 * Created by bitware on 10/7/17.
 */

public class OrderDetailsFragment extends Fragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ViewSwitcher.ViewFactory {
    View view;
    ListView lv_orderDetails;
    AdapterOrderDetail adapterOrderDetail;
    String orderStatusText = "", orderId = "", orderStatus = "", deliverContact = "", deliveryUserLat = "", deliveryUserlng = "";
    ImageView ivOrderStaus, imgCurrentLocation;
    TextView tvOrderStatustext, tv_back_orders_nnn, tvGetDirection;
    ArrayList<ModelOrderDetail> arrOrderDetails;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    GoogleMap googleMap;
    private double deliveryCurrentLat, deliveryCurrentLang;
    View header, footer;
    TextView tv_orderId, tv_delivererName;
    CircleImageView iv_deliverImage;
    LinearLayout ll_contact;
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    String userLat = null, userLang = null;
    boolean isZoomedMap = true;
    Bundle mSavedInstance;
    boolean isMapShown = true, isGPSEnabled = false, isResumeCalled = false;
    //LinearLayout llMapView;
    CustomScrollView mScrollView;
    Marker myMarker, myMarkerUser;
    ScrollView scrollView;
    CustomMapView mapView;
    Marker deliveryMarker = null, userMarker;
    LatLng receivedUserLatLng;
    ////////////////////
    double mLat, mLong;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.activity_order_details, container, false);
            header = getActivity().getLayoutInflater().inflate(R.layout.header_orderdetail, null);
            footer = getActivity().getLayoutInflater().inflate(R.layout.footer_orderdetail, null);
            Config.editorFlag = "Yes";
            inIt();
            mSavedInstance = savedInstanceState;
        }
        return view;
    }

    //initialization
    private void inIt() {
        sharedPreferences = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(getActivity());
        imgCurrentLocation = (ImageView) footer.findViewById(R.id.imgCurrentLocation);
        isInternetPresent = cd.isConnectingToInternet();
        tvGetDirection = (TextView) footer.findViewById(R.id.tvGetDirection);
        lv_orderDetails = (ListView) view.findViewById(R.id.lv_orderDetails);
        scrollView = (ScrollView) footer.findViewById(R.id.scrollMap);
        lv_orderDetails.addHeaderView(header);
        lv_orderDetails.addFooterView(footer);
        ivOrderStaus = (ImageView) footer.findViewById(R.id.ivOrderStaus);
        tvOrderStatustext = (TextView) footer.findViewById(R.id.tvOrderStatustext);
        // llMapView = (LinearLayout) footer.findViewById(R.id.llMapView);
        tv_orderId = (TextView) view.findViewById(R.id.tv_orderId);
        iv_deliverImage = (CircleImageView) footer.findViewById(R.id.iv_deliverImage);
        tv_delivererName = (TextView) footer.findViewById(R.id.tv_delivererName);
        tv_back_orders_nnn = (TextView) view.findViewById(R.id.tv_back_orders_nnn);
        ll_contact = (LinearLayout) view.findViewById(R.id.ll_contact);

        tv_orderId.setText("Order " + sharedPreferences.getString("order_id", ""));
        orderId = sharedPreferences.getString("order_id", "");
        isGPSEnabled = checkGPSEnabled();

        tvGetDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customerReceivedLat = sharedPreferences.getString("customerLat", "");
                String customerReceivedLong = sharedPreferences.getString("customerLong", "");

                //Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+customerReceivedLat+","+customerReceivedLong+"&daddr="+deliveryCurrentLat+","+deliveryCurrentLang));
                //intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                //startActivity(intent);

                //String newUri = "comgooglemaps://?center=%f,%f&q=%f,%f";
                System.out.println("Customer Lat >" +customerReceivedLat + "long > " +customerReceivedLong);
                String uri = "http://maps.google.com/maps?center=" + customerReceivedLat + "," + customerReceivedLong + "&q=" + customerReceivedLat + "," + customerReceivedLong;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    } catch (ActivityNotFoundException innerEx) {
                        Toast.makeText(getActivity(), "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if (isGPSEnabled) {
            AfterGPSEnabled();
        } else {
            showSettingsAlert();
        }

        imgCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getCurrentLocation();


            }
        });
        //showMap(mSavedInstance);

    }

    private void AfterGPSEnabled() {
        if (isInternetPresent) {
            new OrderDetails().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"order_id\":\"" + orderId + "\"}");
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
        String mNavigation = sharedPreferences.getString("navigation", "");
        if (mNavigation.equals("fromUpcomingOrder")) {
            ll_contact.setVisibility(View.VISIBLE);
            orderStatusText = sharedPreferences.getString("orderStatus", "");
            System.out.println(">>> Upcoming Received Status ; " + orderStatusText);

            if (orderStatusText.equalsIgnoreCase("approved")) {
                ivOrderStaus.setImageResource(R.drawable.order_accepted);
                tvOrderStatustext.setText("Food Being Prepared");
            } else if (orderStatusText.equalsIgnoreCase("foodready")) {
                ivOrderStaus.setImageResource(R.drawable.food_ready);
                tvOrderStatustext.setText("On the way");
            } else if (orderStatusText.equalsIgnoreCase("on the way")) {
                ivOrderStaus.setImageResource(R.drawable.on_the_way);
                tvOrderStatustext.setText("Confirm Delivery");
            }

        } else if (mNavigation.equalsIgnoreCase("fromOrderHistory")) {
            System.out.println(">>> History Received Order Status ; " + orderStatusText);

            ll_contact.setVisibility(View.GONE);
            ivOrderStaus.setImageResource(R.drawable.on_the_way);
            tvOrderStatustext.setText("Confirmed Delivery");
        } else if (mNavigation.equalsIgnoreCase("Notification")) {

            orderStatusText = sharedPreferences.getString("orderStatus", "");
            System.out.println(">>> Notifcation  Status ; " + orderStatusText);
            if (orderStatusText.equalsIgnoreCase("approved")) {
                ivOrderStaus.setImageResource(R.drawable.order_accepted);
                tvOrderStatustext.setText("Food Being Prepared");
            } else if (orderStatusText.equalsIgnoreCase("foodready")) {
                ivOrderStaus.setImageResource(R.drawable.food_ready);
                tvOrderStatustext.setText("On the way");
            } else if (orderStatusText.equalsIgnoreCase("on the way")) {
                ivOrderStaus.setImageResource(R.drawable.on_the_way);
                tvOrderStatustext.setText("Confirm Delivery");
            } else if (orderStatus.equalsIgnoreCase("delivered")) {
                ll_contact.setVisibility(View.GONE);
                ivOrderStaus.setImageResource(R.drawable.on_the_way);
                tvOrderStatustext.setText("Confirmed Delivery");
            } else {
                ll_contact.setVisibility(View.GONE);
                ivOrderStaus.setImageResource(R.drawable.on_the_way);
                tvOrderStatustext.setText("Confirmed Delivery");

            }
        }

        tvOrderStatustext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderStatus = tvOrderStatustext.getText().toString();
                System.out.println("Order Status >>> " + orderStatus);
                if (!orderStatus.equals("Confirmed Delivery")) {
                    if (orderStatus.equalsIgnoreCase("Food Being Prepared")) {
                        orderStatus = "foodready";
                        callUpdateStatusAPI();
                    } else if (orderStatus.equalsIgnoreCase("On the way")) {
                        orderStatus = "on the way";
                        callUpdateStatusAPI();
                    } else if (orderStatus.equalsIgnoreCase("Confirm Delivery")) {
                        orderStatus = "delivered";
                        askConfirmationDialiog();
                    }
                }
            }
        });


        tv_back_orders_nnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.isFROM.equalsIgnoreCase("Orders")) {
                    editor.putString("tabPosition", "0");
                    editor.commit();
                } else {
                    editor.putString("tabPosition", "1");
                    editor.commit();
                }

                startActivity(new Intent(getActivity(), ActivityDasshboard.class));
            }
        });


        ll_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle("Call");
                builder.setMessage("Are you sure you want to call?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                checkCallingPermission();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                android.support.v7.app.AlertDialog alert = builder.create();
                alert.show();

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isResumeCalled) {
            AfterGPSEnabled();
        }
    }


    //confirm delivery
    private void askConfirmationDialiog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Delivery?");
        builder.setMessage("Confirm delivery and send receipt to the customer?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callUpdateStatusAPI();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();

    }

    //update order status API
    private void callUpdateStatusAPI() {
        System.out.println("Order Status After Condition >>> " + orderStatus);
        String session_userToken = sharedPreferences.getString("session_usertoken", "");
        if (isInternetPresent) {
            new UpdateOrderStatus().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"order_id\":\"" + orderId + "\",\"order_status\":\"" + orderStatus + "\",\"session_user_token\":\"" + session_userToken + "\"}");
        } else {
            Toast.makeText(getActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCallingPermission() {
        if (deliverContact != null || deliverContact.equalsIgnoreCase("null") || deliverContact.equalsIgnoreCase("")) {
            if (checkPermission(Manifest.permission.CALL_PHONE)) {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + deliverContact)));
            }
            if (!checkPermission(Manifest.permission.CALL_PHONE)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
            }
        } else {
            Toast.makeText(getActivity(), "Contact no is not available for deliverer.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMap(Bundle savedInstanceState) {
        mapView = (CustomMapView) footer.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.setDuplicateParentStateEnabled(false);
        mapView.onResume();
        googleMap = mapView.getMap();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMyLocationChangeListener(myLocationChangeListener());
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

        //getCurrentLocation();
    }

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener() {
        return new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                String customerReceivedLat = null, customerReceivedLong = null;
                if (isMapShown) {
                    deliveryCurrentLat = location.getLatitude();
                    deliveryCurrentLang = location.getLongitude();
                    System.out.println("*********delivery boy********" + deliveryCurrentLat + "*" + deliveryCurrentLang);
                    LatLng currentDeliveryPosition = new LatLng(deliveryCurrentLat, deliveryCurrentLang);
                    if (deliveryMarker != null) {
                        deliveryMarker.remove();
                    }
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentDeliveryPosition));
                    //addDeliveryMarker(currentDeliveryPosition);

                    customerReceivedLat = sharedPreferences.getString("customerLat", "");
                    customerReceivedLong = sharedPreferences.getString("customerLong", "");

                    if (customerReceivedLat != null && !customerReceivedLat.equals("") && !customerReceivedLat.equals("null")) {
                        receivedUserLatLng = new LatLng(Double.parseDouble(customerReceivedLat), Double.parseDouble(customerReceivedLong));
////////////////////////////////
                        if (userMarker != null) {
                            userMarker.remove();
                        } if (userMarker != null) {
                            userMarker.remove();
                        }
                        /////////////////////////////////
                        addUserMarkere(receivedUserLatLng);
                    }
                    if (isZoomedMap) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(deliveryCurrentLat, deliveryCurrentLang), 17));
                    }
                    isZoomedMap = false;

                    System.out.println("## While drwing path : " + currentDeliveryPosition.longitude + ">> " + currentDeliveryPosition.latitude + "--" + receivedUserLatLng.latitude + "" + receivedUserLatLng.longitude);
                   /* String directionURL =  getMapsApiDirectionsUrl(currentDeliveryPosition,receivedUserLatLng);
                    ReadTask downloadTask = new ReadTask();
                    downloadTask.execute(directionURL);*/
                    //isMapShown = false;
                }
            }
        };
    }

    private void addDeliveryMarker(LatLng deliverylatLong) {

        deliveryMarker = googleMap.addMarker(new MarkerOptions()
                .position(deliverylatLong)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.greenmap))
                .snippet("Deliverer Current Location")
                .title("Current"));
    }

    private void addUserMarkere(LatLng userLatLng) {

        userMarker = googleMap.addMarker(new MarkerOptions()
                .position(userLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.greymap))
                .snippet("User Current Location")
                .title("User"));
    }

    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // TODO Auto-generated method stub
            String data = "";
            try {
                MapHttpConnection http = new MapHttpConnection();
                data = http.readUr(url[0]);

            } catch (Exception e) {
                // TODO: handle exception
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

    }

    public class MapHttpConnection {
        @SuppressLint("LongLogTag")
        public String readUr(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream istream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                istream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(istream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();

            } catch (Exception e) {
                Log.d("Exception while reading url", e.toString());
            } finally {
                istream.close();
                urlConnection.disconnect();
            }
            return data;

        }
    }

    public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;

        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            // TODO Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(4);
                polyLineOptions.color(Color.BLUE);
            }
            if (polyLineOptions != null) {
                googleMap.addPolyline(polyLineOptions);

            }

        }
    }

    @Override
    public View makeView() {
        return null;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                }
                return;
        }
    }


    class UpdateOrderStatus extends AsyncTask<String, Void, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
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
                    .url(Config.BASE_URL + "updateorder")
                    .post(body)
                    .build();

            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>>update status result :" + s);
            p.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");

                    if (is_success == true) {
                        if (!tvOrderStatustext.getText().toString().equalsIgnoreCase("Confirm Delivery")) {
                            Toast.makeText(getActivity(), "Status Updated Successfully..", Toast.LENGTH_SHORT).show();
                        }
                        orderStatus = tvOrderStatustext.getText().toString();
                        if (orderStatus.equals("Food Being Prepared")) {
                            ivOrderStaus.setImageResource(R.drawable.food_ready);
                            tvOrderStatustext.setText("On the way");
                        }
                        if (orderStatus.equalsIgnoreCase("On the way")) {
                            ivOrderStaus.setImageResource(R.drawable.on_the_way);
                            tvOrderStatustext.setText("Confirm Delivery");
                        } else if (orderStatus.equalsIgnoreCase("Confirm Delivery")) {
                            Toast.makeText(getActivity(), "Your order is successfully delivered", Toast.LENGTH_SHORT).show();
                            tvOrderStatustext.setText("Confirmed Delivery");
                        }
                    } else {
                        p.dismiss();
                        Toast.makeText(getActivity(), jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
                Toast.makeText(getActivity(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class OrderDetails extends AsyncTask<String, Void, String> {

        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
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
                    .url(Config.BASE_URL + "orderdetails")
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // clearEditTextData();
            System.out.println(">>>Orders details result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");

                    if (is_success == true) {
                        JSONArray menudetails = jsonObject.getJSONArray("menudetails");
                        arrOrderDetails = new ArrayList<>();
                        for (int i = 0; i < menudetails.length(); i++) {
                            JSONObject menuObj = menudetails.getJSONObject(i);
                            ModelOrderDetail modelOrderDetail = new ModelOrderDetail();
                            modelOrderDetail.setProductName(menuObj.getString("menu_name"));
                            modelOrderDetail.setProductQty(menuObj.getString("menu_qty"));
                            arrOrderDetails.add(modelOrderDetail);
                        }
                        adapterOrderDetail = new AdapterOrderDetail(getActivity(), R.layout.row_orderdetail, arrOrderDetails);
                        lv_orderDetails.setAdapter(adapterOrderDetail);

                        //display deliverer info
                        JSONObject delivery_user = jsonObject.getJSONObject("orderdetails");
                        tv_delivererName.setText(delivery_user.getString("customer_name"));
                        String mString = delivery_user.getString("customer_pic");
                        if (mString != null || !mString.isEmpty()) {
                            Glide.with(getActivity()).load(delivery_user.getString("customer_pic")).into(iv_deliverImage);
                        } else {
                            Glide.with(getActivity()).load(R.drawable.blank_resturant).into(iv_deliverImage);
                        }
                        deliverContact = delivery_user.getString("customer_phone");
                        JSONArray customer_location = jsonObject.getJSONArray("customer_location");
                        JSONObject custObject = customer_location.getJSONObject(0);

                        editor.putString("customerLat", custObject.getString("latitude"));
                        editor.putString("customerLong", custObject.getString("longitude"));
                        editor.commit();

                        showMap(mSavedInstance); // After getting lat long map is initialized

                    } else {
                        p.dismiss();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
                Toast.makeText(getActivity(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void inserUserLocationAPI() {
        GPSTracker gpsTracker = new GPSTracker(getActivity());
        double dLat = 0.0, dLang = 0.0;
        String firstTimeLat = "", firstTimeLang = "";

        dLat = gpsTracker.getLatitude();
        dLang = gpsTracker.getLongitude();
        if (!String.valueOf(dLat).isEmpty() && dLat != 0.0 && !String.valueOf(dLat).equalsIgnoreCase(null) && !String.valueOf(dLat).equalsIgnoreCase("null")) {
           /* firstTimeLat = getFormatedLatLong(dLat);
            firstTimeLang = getFormatedLatLong(dLang);*/
            firstTimeLat = String.valueOf(dLat);
            firstTimeLang = String.valueOf(dLang);
        }

        System.out.println(">>> User first time Lat Long :" + dLat + "--" + dLang);
        JSONObject loationObj = new JSONObject();
        try {
            if (!firstTimeLat.equalsIgnoreCase("") && !firstTimeLang.equalsIgnoreCase("")) {
                editor.putString("firstTimeLat", firstTimeLat);
                editor.putString("firstTimeLang", firstTimeLang);
                editor.commit();
            }
            loationObj.put("latitude", firstTimeLat);
            loationObj.put("longitude", firstTimeLang);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray locationArray = new JSONArray();
        locationArray.put(loationObj);

        if (isInternetPresent) {
            new updateLocationAPI().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"session_user_token\":\"" + sharedPreferences.getString("session_usertoken", "") + "\",\"location_details\":" + locationArray + " }");
        } else {
            Toast.makeText(getActivity(), R.string.noNetworkMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormatedLatLong(double currentLat) {
        String strLat = "";
        String[] arr = String.valueOf(currentLat).split("\\.");
        Long[] longArr = new Long[2];

        longArr[0] = Long.parseLong(arr[0]); // 1
        longArr[1] = Long.parseLong(arr[1]); //

        String mainDigits = String.valueOf(longArr[0]);
        String strLenght = String.valueOf(longArr[1]);
        String first4char = strLenght.substring(0, 4);
        String strFinal = mainDigits + "." + first4char;
        System.out.println(">>>> return val --- :" + strFinal);
        return strFinal;
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

    class updateLocationAPI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Location Update params :" + params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL + "updateuserlocation")
                    .post(body)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d("response123", String.valueOf(response));
                return response.body().string();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(">>>Delivery Boy Backend Location Udate Result :" + s);
            try {
                if (s != null) {
                    JSONObject jsonObject = new JSONObject(s);
                    // Toast.makeText(getActivity(), jsonObject.getString("is_success")+"-"+jsonObject.getString("err_msg"), Toast.LENGTH_SHORT).show();

                }
                // showMap(mSavedInstance);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void getCurrentLocation() {

        System.out.println("**********");
        GPSTracker mGPS = new GPSTracker(getActivity());

        if (mGPS.canGetLocation) {


            mGPS = new GPSTracker(getActivity());
            mLat = mGPS.getLatitude();
            mLong = mGPS.getLongitude();
            System.out.println("**********" + mLat + "**" + mLong);
            //setMyLocation = true;
            LatLng cur_Latlng = new LatLng(mLat, mLong); // giving your marker to zoom to your location area.
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(cur_Latlng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));

            String customerReceivedLat = null, customerReceivedLong = null;
            if (isMapShown) {
                deliveryCurrentLat = mLat;
                deliveryCurrentLang = mLong;
                System.out.println("*********delivery boy********" + deliveryCurrentLat + "*" + deliveryCurrentLang);
                LatLng currentDeliveryPosition = new LatLng(deliveryCurrentLat, deliveryCurrentLang);
                if (deliveryMarker != null) {
                    deliveryMarker.remove();
                }
               // addDeliveryMarker(currentDeliveryPosition);

                customerReceivedLat = sharedPreferences.getString("customerLat", "");
                customerReceivedLong = sharedPreferences.getString("customerLong", "");

                if (customerReceivedLat != null && !customerReceivedLat.equals("") && !customerReceivedLat.equals("null")) {
                    receivedUserLatLng = new LatLng(Double.parseDouble(customerReceivedLat), Double.parseDouble(customerReceivedLong));
                    addUserMarkere(receivedUserLatLng);
                }
                if (isZoomedMap) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(deliveryCurrentLat, deliveryCurrentLang), 17));
                }
                isZoomedMap = false;

                System.out.println("## While drwing path : " + currentDeliveryPosition.longitude + ">> " + currentDeliveryPosition.latitude + "--" + receivedUserLatLng.latitude + "" + receivedUserLatLng.longitude);
                   /* String directionURL =  getMapsApiDirectionsUrl(currentDeliveryPosition,receivedUserLatLng);
                    ReadTask downloadTask = new ReadTask();
                    downloadTask.execute(directionURL);*/
                //isMapShown = false;
            }
            //moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLong), 17));


        } else {
            Toast.makeText(getActivity(), "unable to get location", Toast.LENGTH_LONG).show();
        }
    }


    public final class GPSTracker implements LocationListener {

        private final Context mContext;

        // flag for GPS status
        public boolean isGPSEnabled = false;

        // flag for network status
        boolean isNetworkEnabled = false;

        // flag for GPS status
        boolean canGetLocation = false;

        Location location; // location
        double latitude; // latitude
        double longitude; // longitude

        // The minimum distance to change Updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        public GPSTracker(Context context) {
            this.mContext = context;
            getLocation();

        }

        /**
         * Function to get the user's current location
         * @return
         */
        public Location getLocation() {
            try {
                System.out.println("In getlocation.............");
                locationManager = (LocationManager) mContext
                        .getSystemService(Context.LOCATION_SERVICE);

                // getting GPS status
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                Log.v("isGPSEnabled", "=" + isGPSEnabled);

                // getting network status
                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

                if (isGPSEnabled == false && isNetworkEnabled == false) {
                    // no network provider is enabled
                } else {
                    this.canGetLocation = true;
                    if (isNetworkEnabled) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);



                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();

                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return location;
        }

        /**
         * Stop using GPS listener Calling this function will stop using GPS in your
         * app
         * */
        public void stopUsingGPS() {
            if (locationManager != null) {
                locationManager.removeUpdates(GPSTracker.this);
            }
        }

        /**
         * Function to get latitude
         * */
        public double getLatitude() {
            if (location != null) {

                latitude = location.getLatitude();
            }

            // return latitude
            return latitude;
        }

        /**
         * Function to get longitude
         * */
        public double getLongitude() {
            if (location != null) {

                longitude = location.getLongitude();
            }

            // return longitude
            return longitude;
        }

        /**
         * Function to check GPS/wifi enabled
         *
         * @return boolean
         * */
        public boolean canGetLocation() {
            return this.canGetLocation;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                System.out.println("lat>>>>>++++++"+ latitude);
                System.out.println("lang>>>>>>+++++++"+ longitude);

                //getCurrentLocation();
                LatLng currentDeliveryPosition = new LatLng(latitude, longitude);
                if (deliveryMarker != null) {
                    deliveryMarker.remove();
                }
               // addDeliveryMarker(currentDeliveryPosition);

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }


}
