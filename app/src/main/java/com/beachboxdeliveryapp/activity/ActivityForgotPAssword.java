package com.beachboxdeliveryapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class used to reset password.
 * Created by on 11/7/17.
 */

public class ActivityForgotPAssword extends AppCompatActivity {

    EditText edtEmail;
    TextView tvSend,tv_back_photo;
    String strEmail="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        init();
        tv_back_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityForgotPAssword.this,SignInActivity.class));
                finish();
            }
        });

        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strEmail = edtEmail.getText().toString();

                if (!strEmail.isEmpty()){
                    if (isInternetPresent){
                        new ForgotPassword().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"username\":\"" + strEmail + "\"}");
                    }else {
                        Toast.makeText(ActivityForgotPAssword.this,R.string.no_internet,Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private void init() {
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(ActivityForgotPAssword.this);
        isInternetPresent = cd.isConnectingToInternet();
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        tvSend = (TextView) findViewById(R.id.tv_send);
        tv_back_photo = (TextView) findViewById(R.id.tv_back_photo);
    }

    //forgot password API
    class ForgotPassword extends AsyncTask<String, Void, String> {

        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(ActivityForgotPAssword.this);
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
                    .url(Config.BASE_URL+"deliveryuser/forgotpassword")
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
            System.out.println(">>> Forgot passsword result :" + s);
            p.dismiss();

            if(s != null){
                try{
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");

                    if(is_success==true){
//                        Toast.makeText(ActivityForgotPAssword.this,"User logged in successfully",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ActivityForgotPAssword.this,SignInActivity.class));
                        finish();
                    }
                    else {
                        p.dismiss();
                        Toast.makeText(ActivityForgotPAssword.this,"Failed to login",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                p.dismiss();
                Toast.makeText(ActivityForgotPAssword.this, "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
