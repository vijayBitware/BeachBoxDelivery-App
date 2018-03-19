package com.beachboxdeliveryapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.domain.GPSTracker;
import com.beachboxdeliveryapp.domain.LocationService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * This class used for registration.
 * Created by bitwarepc on 20-Jun-17.
 */

public class SignInActivity  extends Activity {
    @BindView(R.id.tv_signIn)
    TextView tv_signIn;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edt_password)
    EditText edt_password;
    @BindView(R.id.tvForgotPass)
    TextView tvForgotPass;
    String strEmail = "",strPassword = "";
    public static String strMove = "";
    private String response_msg="",is_success="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    String currentLat="",currentLng="",priviousLat="",priviousLng="";
    GPSTracker gpsTracker;
    String strFCMTokan = "";
    boolean isUpdateDialogShow = false;
    int mVersionCode = 0;
    boolean isGPSEnabled = false;
    boolean isResumeCalled = false;
    String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        ButterKnife.bind(this);
        init();
        checkPermission();
    }

    private void processAfterPermission() {
        if (isInternetPresent) {
        isGPSEnabled = checkGPSEnabled();
            if(isGPSEnabled){
                    callVersionAPI();
            }else{
                showSettingsAlert();
            }
        }else{
            Toast.makeText(SignInActivity.this, getApplicationContext().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }
    }

    //initialization
    private void init() {
        strFCMTokan = FirebaseInstanceId.getInstance().getToken();
        System.out.println(">> On Login FCM tokan id :"+ strFCMTokan );
        edt_password.setTransformationMethod(new PasswordTransformationMethod());
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(SignInActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        tv_signIn = (TextView)findViewById(R.id.tv_signIn);
        tvForgotPass = (TextView)findViewById(R.id.tvForgotPass);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionCode = packageInfo.versionCode;
            System.out.println(">>> mVersionCode "+mVersionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void OnButtonClick() {
        tv_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetPresent) {
                    isGPSEnabled = checkGPSEnabled();
                    if(isGPSEnabled){
                strEmail = edtEmail.getText().toString().trim();
                strPassword = edt_password.getText().toString().trim();
                if(!strEmail.equals("")){
                    if(!strPassword.equals("")){
                        if(isInternetPresent){
                            new LoginTask().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"username\":\"" + strEmail + "\",\"password\":\"" + strPassword + "\",\"device_type\":\"" + Config.DEVICE_TYPE + "\",\"device_push_token\":\"" + strFCMTokan + "\"}");
                        }
                        else {
                            Toast.makeText(SignInActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        edt_password.requestFocus();
                        edt_password.setError("Please enter password");
                    }
                }else{
                    edtEmail.requestFocus();
                    edtEmail.setError("Please enter username");
                }
                    }else{
                        showSettingsAlert();
                    }
                }else{
                    Toast.makeText(SignInActivity.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                }

            }
        });

        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignInActivity.this,ActivityForgotPAssword.class);
                startActivity(i);
                finish();
            }
        });
    }

    //check uploaded version
    private void callVersionAPI() {
        new UpdateAndroidVersion().execute("{\"device_type\":\"" + Config.DEVICE_TYPE + "\",\"version\":\"" + String.valueOf(mVersionCode) + "\"}");
    }


    //check runtime permissions
    private void checkPermission() {
        if(ActivityCompat.checkSelfPermission(SignInActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(SignInActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(SignInActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(SignInActivity.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED ))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[0])
                    ||ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[3]))
            {
                //ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SignInActivity.this);
                builder.setTitle(SignInActivity.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(SignInActivity.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(SignInActivity.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SignInActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(SignInActivity.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SignInActivity.this);
                builder.setTitle(SignInActivity.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(SignInActivity.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(SignInActivity.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Location,Storage permission", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(SignInActivity.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }else {
                //just request the permission
                ActivityCompat.requestPermissions(SignInActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
        }else{
            proceedAfterPermission();
            System.out.println("YOU HAVE PERMISSION PROCESS ");
        }
    }

    private void proceedAfterPermission() {
          processAfterPermission();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Exit Application");
        alertDialogBuilder
                .setMessage("Are you sure you want to exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //login API
    class LoginTask extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(SignInActivity.this);
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
                    .url(Config.BASE_URL+"deliveryuser/login")
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
            System.out.println(">>> Login result :" + s);
            p.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");


                    if(is_success==true){
                        Toast.makeText(SignInActivity.this,"User logged in successfully",Toast.LENGTH_SHORT).show();
                        JSONObject logindetails = jsonObject.getJSONObject("logindetails");
                        editor.putString("session_usertoken",logindetails.getString("session_usertoken"));
                        editor.putString("avatar",logindetails.getString("avatar"));
                        editor.putString("status",logindetails.getString("status"));
                        editor.putString("email",logindetails.getString("email"));
                        editor.putString("username",logindetails.getString("username"));
                        editor.putString("isDeliveryBoyLoggedIn","Yes");
                        editor.commit();

                        //inserUserLocationAPI();


                        if(sharedPreferences.getString("status","").equalsIgnoreCase("1")){
                            editor.putString("tabPosition","0");
                            editor.commit();
                            Intent i = new Intent(SignInActivity.this,ActivityDasshboard.class);
                            startActivity(i);
                            finish();
                        }else{
                            Intent i = new Intent(SignInActivity.this,CapturePhotoActivity.class);
                            startActivity(i);
                            finish();
                        }
                    } else  {
                        String errmsg = jsonObject.getString("err_msg");
                        p.dismiss();
                        Toast.makeText(SignInActivity.this,errmsg,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                p.dismiss();
                Toast.makeText(SignInActivity.this, "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void inserUserLocationAPI() {
        GPSTracker gpsTracker = new GPSTracker(SignInActivity.this);
        double dLat = 0.0, dLang = 0.0;
        String  firstTimeLat = "",firstTimeLang = "";

        dLat = gpsTracker.getLatitude();
        dLang = gpsTracker.getLongitude();
        if( !String.valueOf(dLat).isEmpty()  && dLat != 0.0 && !String.valueOf(dLat).equalsIgnoreCase(null) && !String.valueOf(dLat).equalsIgnoreCase("null")){
            firstTimeLat = String.valueOf(dLat);
            firstTimeLang = String.valueOf(dLang);
        }

        System.out.println(">>> User first time Lat Long :"+dLat+"--"+dLang);
        JSONObject loationObj = new JSONObject();
        try {
            if(!firstTimeLat.equalsIgnoreCase("") && !firstTimeLang.equalsIgnoreCase("")){
                editor.putString("firstTimeLat",firstTimeLat);
                editor.putString("firstTimeLang",firstTimeLang);
                editor.commit();
            }

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
            Toast.makeText(SignInActivity.this,R.string.noNetworkMsg,Toast.LENGTH_SHORT).show();
        }
    }

    class updateLocationAPI extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(SignInActivity.this);
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
            System.out.println(">>> LocationUpdate Result:" + s);

                    if(sharedPreferences.getString("status","").equalsIgnoreCase("1")){
                            editor.putString("tabPosition","0");
                            editor.commit();
                            Intent i = new Intent(SignInActivity.this,ActivityDasshboard.class);
                            startActivity(i);
                            finish();
                        }else{
                            Intent i = new Intent(SignInActivity.this,CapturePhotoActivity.class);
                            startActivity(i);
                            finish();
                        }
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

    class UpdateAndroidVersion extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(SignInActivity.this);
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.show();
        }
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> update Version Api  :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"user/getVersion")
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
            System.out.println(">>> Update version  :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String isSuccess = jsonObject.getString("is_success");
                    if (isSuccess.equalsIgnoreCase("true")) {
                        gpsTracker = new GPSTracker(SignInActivity.this);
                        OnButtonClick();
                        if(sharedPreferences.getString("isDeliveryBoyLoggedIn","").equalsIgnoreCase("Yes")){
                            editor.putString("tabPosition","0");
                            editor.commit();
                            Intent i = new Intent(SignInActivity.this,ActivityDasshboard.class);
                            startActivity(i);
                        }

                    } else {
                        updateAppDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(SignInActivity.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void updateAppDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SignInActivity.this);
        builder.setTitle(SignInActivity.this.getResources().getString(R.string.uodateTitle));
        builder.setMessage(SignInActivity.this.getResources().getString(R.string.updateMsg));
        builder.setCancelable(false);
        builder.setPositiveButton(SignInActivity.this.getResources().getString(R.string.updateOk), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.cancel();
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    isUpdateDialogShow = true;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        builder.setNegativeButton(SignInActivity.this.getResources().getString(R.string.updateCancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);

            }
        });
        builder.show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }
            if(allgranted){
                proceedAfterPermission();
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,permissionsRequired[3]))
            {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SignInActivity.this);
                builder.setTitle(SignInActivity.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(SignInActivity.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(SignInActivity.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SignInActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(SignInActivity.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(SignInActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                if (isResumeCalled) {
                    processAfterPermission();
                }
            }
        }else{
            if (isUpdateDialogShow) {
                Intent i = new Intent(SignInActivity.this, SignInActivity.class);
                startActivity(i);
            }
        }
    }
    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) SignInActivity.this.getSystemService(Context.LOCATION_SERVICE);
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
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(SignInActivity.this);
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

}
