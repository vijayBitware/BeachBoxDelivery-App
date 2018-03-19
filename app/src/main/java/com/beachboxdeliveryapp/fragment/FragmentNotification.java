package com.beachboxdeliveryapp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;


import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.adapter.AdapterNotification;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.model.notificationResponse.Notification;
import com.beachboxdeliveryapp.model.notificationResponse.ResponseNotificationNew;
import com.beachboxdeliveryapp.volly.APIRequest;
import com.beachboxdeliveryapp.volly.BaseResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * This class for displaying list of notifications.
 * Created by bitware on 10/7/17.
 */

public class FragmentNotification extends Fragment implements APIRequest.ResponseHandler {
    View view;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ConnectionDetector cd;
    boolean isInternetPresent;
    ListView lv_notification;
    AdapterNotification adapterNotification ;
    ArrayList<ResponseNotificationNew> arrNoti = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view=inflater.inflate(R.layout.fragment_notification,container,false);
            inIt();

            if (isInternetPresent) {
                APICallForGettingNotifications();
            } else {
                Toast.makeText(getContext(), getActivity().getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
            }
                   /* for (int i = 0; i < 8 ; i++){
                          ResponseNotification  responseNotification = new ResponseNotification();
                          responseNotification.setNotiMsg(""+i);
                          arrNoti.add(responseNotification);
                    }*/

         /*   adapterNotification = new AdapterNotification(getActivity(),R.layout.row_notification,arrNoti);
            lv_notification.setAdapter(adapterNotification);*/

        }
        return view;
    }

    private void APICallForGettingNotifications() {
        JSONObject jsonObject = new JSONObject();
        String sessionUserTokan = sharedPreferences.getString("session_usertoken", "");
        try {
            jsonObject.put("accesstoken", Config.access_token);
            jsonObject.put("session_user_token", sessionUserTokan);
            String notificationURL = Config.BASE_URL + "user/getAllNotification";

            System.out.println(">>> jsonObject :"+jsonObject);

            new APIRequest(getActivity(), jsonObject, notificationURL, this, Config.API_GET_ALLNOTIFICATION, Config.POST);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void inIt() {
        lv_notification = (ListView)view.findViewById(R.id.lv_notification);
        cd = new ConnectionDetector(getActivity());
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    @Override
    public void onSuccess(BaseResponse response) {
        System.out.println(">>> response Notification are :"+response);

        if (response.getApiName() == Config.API_GET_ALLNOTIFICATION) {
            ResponseNotificationNew  notiResponse= (ResponseNotificationNew) response;
            if (notiResponse.getIsSuccess() == true) {
                List<Notification> arrNotiList = notiResponse.getNotification();
                if (arrNotiList.size() > 0) {

                    adapterNotification = new AdapterNotification(getActivity(), R.layout.row_notification, arrNotiList);
                    lv_notification.setAdapter(adapterNotification);

                } else {
                    Toast.makeText(getActivity(), "Notifications are not available yet!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Something went wrong,Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onFailure(BaseResponse response) {

    }
}
