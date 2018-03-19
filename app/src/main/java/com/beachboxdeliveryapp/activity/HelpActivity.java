package com.beachboxdeliveryapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.domain.ConnectionDetector;


public class HelpActivity extends AppCompatActivity {
TextView tv_home;
    Boolean isInternetPresent,isSuccess;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    WebView webView;
    String helpUrl = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        cd = new ConnectionDetector(HelpActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        sharedPreferences = HelpActivity.this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        tv_home = (TextView)findViewById(R.id.tv_home);
        webView = (WebView) findViewById(R.id.webView1);

        helpUrl = sharedPreferences.getString("helpdeliveryuser_url","").trim();

        webView.getSettings().setJavaScriptEnabled(true); // enable javascript
        final Activity activity = this;
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        webView .loadUrl(helpUrl);
       // setContentView(mWebview );

        tv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
