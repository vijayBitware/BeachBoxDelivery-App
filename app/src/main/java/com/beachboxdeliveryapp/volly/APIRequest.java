package com.beachboxdeliveryapp.volly;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.model.historyOrder.ModelVHistoryOrder;
import com.beachboxdeliveryapp.model.notificationResponse.LogOut;
import com.beachboxdeliveryapp.model.notificationResponse.ResponseNotificationNew;
import com.beachboxdeliveryapp.model.upcomingOrder.ModelVUpcomingOrder;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by bitwarepc on 04-Jul-17.
 */

public class APIRequest extends AppCompatActivity {

    private JSONObject mJsonObject;
    private String mUrl;
    private ResponseHandler responseHandler;
    private int API_NAME;
    private Context mContext;
    ProgressDialog progressDialog;
    BaseResponse baseResponse;

    public APIRequest(Context context, JSONObject jsonObject, String url, ResponseHandler responseHandler1, int api, String methodName) {
        this.responseHandler = responseHandler1;
        this.API_NAME = api;
        this.mUrl = url;
        this.mJsonObject = jsonObject;
        this.mContext = context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if (methodName.equals(Config.GET)) {
            apiGetRequest();
        } else {
            apiPostRequest();
        }
    }

    private void apiPostRequest() {

        String REQUEST_TAG = String.valueOf(API_NAME);
        System.out.println("*******apiPostRequest************"+REQUEST_TAG);
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(mUrl, mJsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("response is " + response);
                        setResponseToBody(response);
                        progressDialog.hide();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
            }
        });

        jsonObjectReq.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppSingleton.getInstance(mContext).addToRequestQueue(jsonObjectReq, REQUEST_TAG);
    }

    private void apiGetRequest() {
        String REQUEST_TAG = String.valueOf(API_NAME);
        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(mUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override                    public void onResponse(JSONObject response) {
                        System.out.println("response is " + response);
                        setResponseToBody(response);
                        progressDialog.hide();

                    }
                }, new Response.ErrorListener() {
            @Override            public void onErrorResponse(VolleyError error) {
                progressDialog.hide();
            }
        });

        jsonObjectReq.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        AppSingleton.getInstance(mContext).addToRequestQueue(jsonObjectReq, REQUEST_TAG);
    }

    private void setResponseToBody(JSONObject response) {
        Gson gson = new Gson();
        switch (API_NAME) {
            case Config.API_LOGOUT :
                baseResponse = gson.fromJson(response.toString(), LogOut.class);
                break;


            case Config.API_UPCOMING_ORDER :
                baseResponse = gson.fromJson(response.toString(), ModelVUpcomingOrder.class);
                break;

            case Config.API_ORDER_HISTORY :
                baseResponse = gson.fromJson(response.toString(), ModelVHistoryOrder.class);
                break;

            case Config.API_GET_ALLNOTIFICATION:
                System.out.println(">>>> response.toString() Notification :"+response.toString());
                baseResponse = gson.fromJson(response.toString(), ResponseNotificationNew.class);
                break;


        }
        baseResponse.setApiName(API_NAME);
        responseHandler.onSuccess(baseResponse);
    }


    public interface ResponseHandler {
        public void onSuccess(BaseResponse response);

        public void onFailure(BaseResponse response);

    }
}