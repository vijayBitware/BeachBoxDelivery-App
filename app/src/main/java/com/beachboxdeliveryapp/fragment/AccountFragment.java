package com.beachboxdeliveryapp.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.activity.ActivityDasshboard;
import com.beachboxdeliveryapp.activity.HelpActivity;
import com.beachboxdeliveryapp.activity.SignInActivity;
import com.beachboxdeliveryapp.adapter.AdapterNotification;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.model.notificationResponse.LogOut;
import com.beachboxdeliveryapp.model.notificationResponse.Notification;
import com.beachboxdeliveryapp.model.notificationResponse.ResponseNotificationNew;
import com.beachboxdeliveryapp.volly.APIRequest;
import com.beachboxdeliveryapp.volly.BaseResponse;
import com.bumptech.glide.Glide;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;


/**
 * This class for displaying user profile.
 * Created by bitware on 10/7/17.
 */

public class AccountFragment extends Fragment implements APIRequest.ResponseHandler {

    View view;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    String session_usertoken = "";
    ImageView iv_profileImage;
    TextView tv_userName, tv_email, tv_logout, tv_edit;
    TextView tvHelp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_account, container, false);
            init();
            session_usertoken = sharedPreferences.getString("session_usertoken", "");
            if (isInternetPresent) {
                new ProfileDetails().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"session_usertoken\":\"" + session_usertoken + "\"}");
            } else {
                Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
            tv_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLogoutDialog();
                }
            });
            tv_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ActivityDasshboard) getActivity()).replaceFragment(new FragmentUpdateAccount());
                    // startActivity(new Intent(getContext(), ActivityUpdateProfile.class));
                }
            });
            tvHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), HelpActivity.class));
                }
            });
        }

        return view;
    }

    //initialization
    private void init() {
        sharedPreferences = getContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();
        iv_profileImage = (ImageView) view.findViewById(R.id.iv_profileImage);
        tv_userName = (TextView) view.findViewById(R.id.tv_userName);
        tv_email = (TextView) view.findViewById(R.id.tv_email);
        tv_logout = (TextView) view.findViewById(R.id.tv_logout);
        tv_edit = (TextView) view.findViewById(R.id.tv_edit);
        tvHelp = (TextView) view.findViewById(R.id.tvHelp);

    }

    @Override
    public void onSuccess(BaseResponse response) {
        LogOut logoutResponse = (LogOut) response;
        if (logoutResponse.getIsSuccess()) {

            Toast.makeText(getActivity(), logoutResponse.getErrMsg().toString(), Toast.LENGTH_SHORT).show();

            editor.clear();
            editor.putString("isDeliveryBoyLoggedIn", "No");
            editor.commit();
            startActivity(new Intent(getContext(), SignInActivity.class));
        } else {
            Toast.makeText(getActivity(), logoutResponse.getErrMsg().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(BaseResponse response) {

    }

    //get profile details
    class ProfileDetails extends AsyncTask<String, Void, String> {

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
                    .url(Config.BASE_URL + "user/profiledetails")
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
            System.out.println(">>> Profile result :" + s);
            p.dismiss();

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    boolean is_success = jsonObject.getBoolean("is_success");

                    if (is_success == true) {
                        String firstName = jsonObject.getString("first_name");
                        String lastName = jsonObject.getString("last_name");
                        String customerEmail = jsonObject.getString("email");
                        String contact = jsonObject.getString("customer_phone");
                        String customerPic = jsonObject.getString("customer_pic");
                        Glide.with(getContext()).load(customerPic).into(iv_profileImage);

                        tv_userName.setText(jsonObject.getString("customer_name"));
                        tv_email.setText(customerEmail + " . " + contact);
                        //  tv_profilePhone.setText(contact);
                        editor.putString("firstName", firstName);
                        editor.putString("lastName", lastName);
                        editor.putString("email", customerEmail);
                        editor.putString("contact", contact);
                        editor.putString("customerPic", customerPic);
                        editor.putString("helpdeliveryuser_url", jsonObject.getString("help_delivery_user"));
                        editor.commit();
                    } else {
                        p.dismiss();
                        Toast.makeText(getContext(), "Something went to wrong, please try again later.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
                Toast.makeText(getContext(), "Network error.try again later...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext())
                .setTitle("Logout?")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        logOut();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void logOut() {
        JSONObject jsonObject = new JSONObject();
        String sessionUserTokan = sharedPreferences.getString("session_usertoken", "");
        try {
            jsonObject.put("accesstoken", Config.access_token);
            jsonObject.put("session_user_token", sessionUserTokan);
            String logOutURL = Config.BASE_URL + "getLogout";

            System.out.println(">>> jsonObject :" + jsonObject);

            new APIRequest(getActivity(), jsonObject, logOutURL, this, Config.API_LOGOUT, Config.POST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
