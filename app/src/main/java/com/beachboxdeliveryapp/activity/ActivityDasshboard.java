package com.beachboxdeliveryapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.fragment.AccountFragment;
import com.beachboxdeliveryapp.fragment.FragmentNotification;
import com.beachboxdeliveryapp.fragment.OrdersFragment;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bitware on 10/7/17.
 */

public class ActivityDasshboard extends AppCompatActivity {

    private BottomBar mBottomBar;
    private FragNavController fragNavController;
    //indices to fragments
    private final int TAB_FIRST = FragNavController.TAB1;
    private final int TAB_SECOND = FragNavController.TAB2;
    private final int TAB_THREE = FragNavController.TAB3;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isInternetPresent;
    int position;
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

    Bundle mSavedInstanceState;
    ConnectionDetector cd;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mSavedInstanceState = savedInstanceState;
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        cd = new ConnectionDetector(ActivityDasshboard.this);
        isInternetPresent = cd.isConnectingToInternet();
        checkPermission();

            if(Config.notiFlag.equalsIgnoreCase("yes")){
                editor.putString("tabPosition","1");
                editor.commit();
                Config.notiFlag = "No";
            }
        System.out.println(">>>> Received Postion :"+position);

        if (isInternetPresent) {
            isGPSEnabled = checkGPSEnabled();
            if(isGPSEnabled){
                if(position == 0){
                    System.out.println(">>>>  in FIRST:");

                    fragNavController.switchTab(TAB_FIRST);
                }else if(position == 1){
                    System.out.println(">>>>  in SECOND:");

                    fragNavController.switchTab(TAB_SECOND);

                }else if(position == 2){

                    System.out.println(">>>>  in THIIRD:");

                    fragNavController.switchTab(TAB_THREE);
                }else{
                    System.out.println(">>>>  in FIRST DEFAILT:");

                    fragNavController.switchTab(TAB_FIRST);
                }

            }else{
                showSettingsAlert();
            }
        }else{
            Toast.makeText(ActivityDasshboard.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
        }

    }

    private void checkPermission() {
        if(ActivityCompat.checkSelfPermission(ActivityDasshboard.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(ActivityDasshboard.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(ActivityDasshboard.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED )
                || (ActivityCompat.checkSelfPermission(ActivityDasshboard.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED ))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[0])
                    ||ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[3]))
            {
                //ActivityCompat.requestPermissions(ActivityHome.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityDasshboard.this);
                builder.setTitle(ActivityDasshboard.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivityDasshboard.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivityDasshboard.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivityDasshboard.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivityDasshboard.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityDasshboard.this);
                builder.setTitle(ActivityDasshboard.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivityDasshboard.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivityDasshboard.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
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
                builder.setNegativeButton(ActivityDasshboard.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }else {
                //just request the permission
                ActivityCompat.requestPermissions(ActivityDasshboard.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
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
        isGPSEnabled = checkGPSEnabled();
        if(isGPSEnabled){
            AfterGettingPermission(mSavedInstanceState);
        }else{
            showSettingsAlert();
        }
    }

    private void AfterGettingPermission(@Nullable Bundle savedInstanceState) {
        List<Fragment> fragments = new ArrayList<>(4);
        fragments.add(new OrdersFragment());
        fragments.add(new FragmentNotification());
        fragments.add(new AccountFragment());
        //link fragments to container
        fragNavController = new FragNavController(getSupportFragmentManager(), R.id.container,fragments);

        position = Integer.parseInt(sharedPreferences.getString("tabPosition",""));
        System.out.println(" >> Received positon : "+position );
        //BottomBar menu
        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottombar_menu);
        mBottomBar.setDefaultTabPosition(position);

        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                //switch between tabs

                switch (menuItemId) {
                    case R.id.bottomBarItemOne:
                        if (isInternetPresent) {
                            isGPSEnabled = checkGPSEnabled();
                            if(isGPSEnabled){
                                if(Config.editorFlag.equalsIgnoreCase("No")){
                                    System.out.println(">>>> in click one  ");
                                    editor.putString("tabPosition", "0");
                                    editor.commit();
                                    fragNavController.switchTab(TAB_FIRST);
                                }else{
                                    editor.putString("tabPosition", "0");
                                    editor.commit();
                                    Config.editorFlag = "No";
                                    startActivity(new Intent(ActivityDasshboard.this, ActivityDasshboard.class));
                                }
                            }else{
                                showSettingsAlert();
                            }
                        }else{
                            Toast.makeText(ActivityDasshboard.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

                        }
                        break;
                    case R.id.bottomBarItemSecond:
                        if (isInternetPresent) {
                            isGPSEnabled = checkGPSEnabled();
                            if(isGPSEnabled){

                                if(Config.editorFlag.equalsIgnoreCase("No")){
                                    System.out.println(">>>> in click two ");
                                    editor.putString("tabPosition", "1");
                                    editor.commit();
                                    fragNavController.switchTab(TAB_SECOND);
                                }else{
                                    editor.putString("tabPosition", "1");
                                    editor.commit();
                                    Config.editorFlag = "No";
                                    startActivity(new Intent(ActivityDasshboard.this, ActivityDasshboard.class));
                                }
                            }else{
                                showSettingsAlert();
                            }
                        }else{
                            Toast.makeText(ActivityDasshboard.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.bottomBarItemThree:
                        if (isInternetPresent) {
                            isGPSEnabled = checkGPSEnabled();
                            if(isGPSEnabled){

                                if(Config.editorFlag.equalsIgnoreCase("No")){
                                    System.out.println(">>>> in click three ");
                                    editor.putString("tabPosition", "2");
                                    editor.commit();
                                    fragNavController.switchTab(TAB_THREE);
                                }else{
                                    editor.putString("tabPosition", "2");
                                    editor.commit();
                                    Config.editorFlag = "No";
                                    startActivity(new Intent(ActivityDasshboard.this, ActivityDasshboard.class));
                                }
                            }else{
                                showSettingsAlert();
                            }
                        }else{
                            Toast.makeText(ActivityDasshboard.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarItemOne) {
                    fragNavController.clearStack();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
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
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(ActivityDasshboard.this,permissionsRequired[3]))
            {

                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ActivityDasshboard.this);
                builder.setTitle(ActivityDasshboard.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(ActivityDasshboard.this.getResources().getString(R.string.storagePermission));
                builder.setPositiveButton(ActivityDasshboard.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ActivityDasshboard.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(ActivityDasshboard.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
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


    public void popAll(){
        int count = getFragmentManager().getBackStackEntryCount();
        System.out.println(">>>> -- "+count);
        if(count > 0){
            System.out.println(">>>> -I m in removing count- "+count);

            for (int i = 0; i < count; i++) {
                getFragmentManager().popBackStack();
            }
        }

    }

    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) ActivityDasshboard.this.getSystemService(Context.LOCATION_SERVICE);
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
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(ActivityDasshboard.this);
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
    public void onBackPressed() {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Exit!");
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory( Intent.CATEGORY_HOME );
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent);


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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(ActivityDasshboard.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                if (isResumeCalled) {
                    //AfterGPSOn();

                    AfterGettingPermission(mSavedInstanceState);
                }
            }
        }
    }



    public void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction =
                    fragmentManager.beginTransaction().addToBackStack(fragment.getClass().toString());
            transaction.replace(R.id.container, fragment);
            transaction.commit();
        }
    }



}
