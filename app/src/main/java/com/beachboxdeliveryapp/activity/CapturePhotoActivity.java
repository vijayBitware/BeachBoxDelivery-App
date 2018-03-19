package com.beachboxdeliveryapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.domain.AndroidMultiPartEntity;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.domain.UploadIamgeClass;
import com.beachboxdeliveryapp.domain.UploadStuff;
import com.beachboxdeliveryapp.fragment.FragmentUpdateAccount;
import com.bumptech.glide.Glide;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.beachboxdeliveryapp.R.id.tv_back_orders_nnn;

/**
 * This class used to capture pictures
 * Created by bitwarepc on 23-Jun-17.
 */

public class CapturePhotoActivity extends Activity{
   /* @BindView(R.id.ivSelectPhoto)
    ImageView ivSelectPhoto;*/
    @BindView(R.id.ivDeliveryBoy)
    ImageView ivDeliveryBoy;
    @BindView(R.id.tv_Next)
    TextView tv_Next;
    @BindView(R.id.tv_back_photo)
    TextView tv_back_photo;
    @BindView(R.id.tvHeaderNew)
    TextView tvHeaderNew;
    Boolean idCamera = false;
    private Uri fileUriId = null;
    private final int SELECT_PHOTO= 1;
    String filePathIdCard = "";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{android.Manifest.permission.CAMERA};
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    ConnectionDetector cd;
    boolean isInternetPresent;
    public String strProfilePicPath = "";
    private static final int CAMERA_REQUEST = 1888;
    boolean isGPSEnabled = false , isResumeCalled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_photo);
        ButterKnife.bind(this);
        init();

        if (isInternetPresent) {
            isGPSEnabled = checkGPSEnabled();
            if(isGPSEnabled){
                OnClickOfNext();
            }else{
                showSettingsAlert();
            }
        }else{
            Toast.makeText(CapturePhotoActivity.this, getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();

        }
    }

    private void OnClickOfNext() {
        tv_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!filePathIdCard.equalsIgnoreCase("")){
                    if(isInternetPresent){
                        new Profile().execute("");
                    }else{
                        Toast.makeText(CapturePhotoActivity.this,getApplicationContext().getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CapturePhotoActivity.this, "Please select the image", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    private void init() {
        Log.e("Token#", FirebaseInstanceId.getInstance().getToken());
        sharedPreferences = getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        cd = new ConnectionDetector(CapturePhotoActivity.this);
        isInternetPresent = cd.isConnectingToInternet();
        tv_back_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String backText = tv_back_photo.getText().toString().trim();
                if(backText.equalsIgnoreCase("Reselect")){
                    selectPic();
                }else  if(backText.equalsIgnoreCase("Back")){
                    Intent i = new Intent(CapturePhotoActivity.this,SignInActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });

    }

    @OnClick(R.id.ivDeliveryBoy)
    public void selectPic(){

        CheckPermission();
        //checkCameraPermission();

    }



   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (idCamera) {
                        filePathIdCard = fileUriId.getPath();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        final Bitmap bitmap = BitmapFactory.decodeFile(filePathIdCard, options);
                        ivDeliveryBoy.setImageBitmap(bitmap);
                        System.out.println(">>> Camera filePathIdCard :"+filePathIdCard);

                    } else {
                        Uri selectedImageUri = data.getData();
                        String imagepath = getPath(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                        ivDeliveryBoy.setImageBitmap(bitmap);
                        filePathIdCard = imagepath;
                        changeText();
                        System.out.println(">>> Galllery filePathIdCard :"+filePathIdCard);


                    }

                    break;
                }
        }
    }*/

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (idCamera) {
                        filePathIdCard = fileUriId.getPath();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        final Bitmap bitmap = BitmapFactory.decodeFile(filePathIdCard, options);
                        ivDeliveryBoy.setImageBitmap(bitmap);
                        System.out.println(">>> filePathIdCard Camera"+filePathIdCard);

                    } else {
                        Uri selectedImageUri = data.getData();
                        String imagepath = getPath(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                        ivDeliveryBoy.setImageBitmap(bitmap);
                        filePathIdCard = imagepath;
                        System.out.println(">>> Galllery uploadImagePath :"+ filePathIdCard);
                    }
                    break;
                }
        }
    }
    private void selectProfilePic() {
        final CharSequence[] options = {"Camera","Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CapturePhotoActivity.this);
        builder.setTitle("Add Picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Camera"))
                {
                    idCamera  = true;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUriId = UploadStuff.getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUriId);
                    startActivityForResult(intent, SELECT_PHOTO);
                }else if (options[item].equals("Gallery"))
                {
                    idCamera  = false;
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void changeText() {
        tv_Next.setVisibility(View.VISIBLE);
        tv_back_photo.setText("Reselect");
        tvHeaderNew.setText("Preview");
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void CheckPermission() {
        if(ActivityCompat.checkSelfPermission(CapturePhotoActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(CapturePhotoActivity.this,permissionsRequired[0])){
                //Show Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(CapturePhotoActivity.this);
                builder.setTitle(CapturePhotoActivity.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(CapturePhotoActivity.this.getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(CapturePhotoActivity.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CapturePhotoActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(CapturePhotoActivity.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(CapturePhotoActivity.this);
                builder.setTitle(CapturePhotoActivity.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(CapturePhotoActivity.this.getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(CapturePhotoActivity.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant  Camera permission", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(CapturePhotoActivity.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(CapturePhotoActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
           proceedAfterPermission();
           /* userId = sharedPreferences.getString("user_id","");
            if (isInternetPresent){
                new getVendorProfile().execute("{\"user_id\":\"" +  userId + "\"}");
            }else {
                Toast.makeText(ActivityProfile.this,"Please Check Your Internet Connection",Toast.LENGTH_SHORT).show();
            }*/
        }

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
            } else if(ActivityCompat.shouldShowRequestPermissionRationale(CapturePhotoActivity.this,permissionsRequired[0])){
//                txtPermissions.setText("Permissions Required");
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(CapturePhotoActivity.this);
                builder.setTitle(CapturePhotoActivity.this.getResources().getString(R.string.Permissionheader));
                builder.setMessage(CapturePhotoActivity.this.getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(CapturePhotoActivity.this.getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CapturePhotoActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(CapturePhotoActivity.this.getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
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

    private void proceedAfterPermission() {
        selectProfilePic();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(CapturePhotoActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission();
            }
        }
    }

    class Profile extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(CapturePhotoActivity.this);
            p.setMessage("In Progress..");
            p.setCanceledOnTouchOutside(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String responseString = null;
            org.apache.http.client.HttpClient httpclient = new org.apache.http.impl.client.DefaultHttpClient();
            org.apache.http.client.methods.HttpPost httppost = new org.apache.http.client.methods.HttpPost(Config.PHOTO_UPLOAD_URL+"user/profile");
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                //publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File fileObj = new File(filePathIdCard);
                entity.addPart("file",  new FileBody(fileObj));
                entity.addPart("session_user_token",  new StringBody(sharedPreferences.getString("session_usertoken","")));
                entity.addPart("accesstoken",  new StringBody(Config.access_token));

                httppost.setEntity(entity);
                HttpResponse response = (HttpResponse) httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();
                responseString = EntityUtils.toString(r_entity);
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            return responseString;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                p.dismiss();
                try {
                  JSONObject object = new JSONObject(s);
                   if(object.getString("is_success").equalsIgnoreCase("true")){
                       editor.putString("tabPosition","1");
                       editor.putString("isProfilePicUploaded","Yes");
                       editor.commit();

                       Intent i = new Intent(CapturePhotoActivity.this,ActivityDasshboard.class);
                       startActivity(i);
                       finish();
                   } else{
                       Toast.makeText(CapturePhotoActivity.this, object.getString("err_msg"), Toast.LENGTH_SHORT).show();
                   }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
            }
        }
    }

    private boolean checkGPSEnabled() {
        boolean resVal = false;
        LocationManager lm = (LocationManager) CapturePhotoActivity.this.getSystemService(Context.LOCATION_SERVICE);
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
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(CapturePhotoActivity.this);
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
    protected void onPostResume() {
        super.onPostResume();
        if (isResumeCalled) {
            OnClickOfNext();
        }
    }
}
