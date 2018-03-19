package com.beachboxdeliveryapp.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beachboxdeliveryapp.R;
import com.beachboxdeliveryapp.activity.ActivityDasshboard;
import com.beachboxdeliveryapp.domain.AndroidMultiPartEntity;
import com.beachboxdeliveryapp.domain.Config;
import com.beachboxdeliveryapp.domain.ConnectionDetector;
import com.beachboxdeliveryapp.domain.UploadStuff;
import com.bumptech.glide.Glide;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import cz.msebera.android.httpclient.client.ClientProtocolException;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


/**
 * This class for updating user profile.
 * Created by bitware on 10/7/17.
 */

public class FragmentUpdateAccount extends Fragment {
View view;
    TextView tvUpdateSave,tv_back,tvUpdateEmail;
    EditText etUpdateFirstName,etUpdateLastName,etUpdatePhone;
    Boolean isInternetPresent;
    ConnectionDetector cd;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String strFirstName = "",strLastName = "",strEmail = "",strPhone = "";
    CircleImageView img_photo_profile;
    int textPos = 0;
    ImageView ivUpload;
    Boolean idCamera = false;
    private Uri fileUriId = null;
    private final int SELECT_PHOTO= 1;
    String uploadImagePath;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    String[] permissionsRequired = new String[]{
            Manifest.permission.CAMERA };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.activity_update_profile, container, false);
            init();

            tvUpdateSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    strFirstName = etUpdateFirstName.getText().toString().trim();
                    strLastName = etUpdateLastName.getText().toString().trim();
                    strEmail = tvUpdateEmail.getText().toString().trim();
                    strPhone = etUpdatePhone.getText().toString().trim();

                    if(isInternetPresent){
                        if (!strFirstName.isEmpty()){
                            if (!strLastName.isEmpty()){
                                if (!strEmail.isEmpty()){
                                    if (strEmail.matches(Config.EMAIL_REGEX)){
                                        if (!strPhone.isEmpty()){
                                            if(strPhone.length() >= 10 && strPhone.length() <= 14){
                                            //if (strPhone.matches(Config.PHONE_REGEX)) {
                                                if (isInternetPresent) {
                                                    String tokenId = sharedPreferences.getString("session_usertoken", "");
                                                    new updateProfile().execute("{\"accesstoken\":\"" + Config.access_token + "\",\"email\":\"" + strEmail + "\",\"first_name\":\"" + strFirstName + "\",\"last_name\":\"" + strLastName + "\",\"phonenumber\":\"" + strPhone + "\",\"session_usertoken\":\"" + tokenId + "\"}");
                                                } else {
                                                    Toast.makeText(getActivity(), getResources().getString(R.string.noNetworkMsg), Toast.LENGTH_SHORT).show();
                                                }
                                            }else {
                                                Toast.makeText(getContext(),"Please Enter Valid Phone Number",Toast.LENGTH_SHORT).show();
                                            }
                                        }else{
                                            etUpdatePhone.requestFocus();
                                            etUpdatePhone.setError("Please enter phone no");                                                        }
                                    }else {
                                   /* etUpdateEmail.requestFocus();
                                    etUpdateEmail.setError("Please enter valid email");*/
                                    }
                                }else {
                               /* etUpdateEmail.requestFocus();
                                etUpdateEmail.setError("Please enter email");*/
                                }
                            }else {
                                etUpdateLastName.requestFocus();
                                etUpdateLastName.setError("Please enter lastname");                                }
                        }else{
                            etUpdateFirstName.requestFocus();
                            etUpdateFirstName.setError("Please enter firstname");
                        }
                    }else{
                        Toast.makeText(getActivity(),getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


       return view;
    }


    private void init() {
        cd = new ConnectionDetector(getActivity());
        isInternetPresent =cd.isConnectingToInternet();
        sharedPreferences = getActivity().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        permissionStatus = getActivity().getSharedPreferences("permissionStatus",MODE_PRIVATE);
        etUpdateFirstName = (EditText)view.findViewById(R.id.etUpdateFirstName);
        etUpdateLastName = (EditText)view.findViewById(R.id.etUpdateLastName);
        tvUpdateEmail = (TextView)view.findViewById(R.id.tvUpdateEmail);
        etUpdatePhone = (EditText)view.findViewById(R.id.etUpdatePhone);
        tvUpdateSave = (TextView)view.findViewById(R.id.tvUpdateSave);
        tv_back = (TextView)view.findViewById(R.id.tv_back);
        ivUpload = (ImageView)view.findViewById(R.id.ivUpload);
        phoneEditText();
        img_photo_profile= (CircleImageView)view. findViewById(R.id.img_photo_profile);

        setValue();

        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();

            }
        });
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("tabPosition","1");
                editor.commit();
                Intent intent = new Intent(getActivity(), ActivityDasshboard.class);
                startActivity(intent);
            }
        });
    }

    private void phoneEditText() {
        etUpdatePhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textPos=etUpdatePhone.getText().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if(etUpdatePhone.getText().length()==3 && textPos!= 4)
                {
                    etUpdatePhone.setText( "("+etUpdatePhone.getText().toString()+")"+ " ");
                    etUpdatePhone.setSelection(6);

                }else if (etUpdatePhone.getText().length()==9 && textPos!=10){
                    etUpdatePhone.setText(etUpdatePhone.getText().toString()+"-");
                    etUpdatePhone.setSelection(10);
                }else if(etUpdatePhone.getText().length()==9 && textPos == 10)
                {
                    System.out.println("*********123*********");
                    String text = etUpdatePhone.getText().delete(8, 9).toString();
                    etUpdatePhone.setText(text);
                    etUpdatePhone.setSelection(8);

                }else if(etUpdatePhone.getText().length() == 5)
                {
                    System.out.println("*********123444444444*********");
                    String text = etUpdatePhone.getText().delete(3, 5).toString();
                    etUpdatePhone.setText(text);
                    str = etUpdatePhone.getText().toString().replaceAll("\\(", "").replaceAll("\\)","");;
                    // String text1 = etUpdatePhone.getText().delete(1, 2).toString();
                    //etUpdatePhone.setText(text1);
                    String[] arr = str.split(" ");
                    String strA = arr[0];
//                    String strnew = arr[1];
                    System.out.println("*********123444444444*****text****"+etUpdatePhone.getText().toString()+"**"+str+"**"+strA);
                    etUpdatePhone.setText(strA.toString());
                    etUpdatePhone.setSelection(2);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void setValue() {
        etUpdateFirstName.setText(sharedPreferences.getString("firstName",""));
        etUpdateLastName.setText(sharedPreferences.getString("lastName",""));
        tvUpdateEmail.setText(sharedPreferences.getString("email",""));


        etUpdatePhone.setText(sharedPreferences.getString("contact",""));
        Glide.with(getActivity()).load(sharedPreferences.getString("customerPic","")).into(img_photo_profile);
    }

    private void proceedAfterPermission() {
        selectProfilePic();
    }


    private void selectProfilePic() {
        final CharSequence[] options = {"Camera","Gallery","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (idCamera) {
                        uploadImagePath = fileUriId.getPath();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        final Bitmap bitmap = BitmapFactory.decodeFile(uploadImagePath, options);
                        img_photo_profile.setImageBitmap(bitmap);

                    } else {
                        Uri selectedImageUri = data.getData();
                        String imagepath = getPath(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                        img_photo_profile.setImageBitmap(bitmap);
                        uploadImagePath = imagepath;
                        System.out.println(">>> Galllery uploadImagePath :"+ uploadImagePath);
                    }

                    if(isInternetPresent){
                        new uploadProfilePic().execute("");
                    }else{
                        Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.noNetworkMsg),Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
        }
    }

    class uploadProfilePic extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
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
                // Adding file data to http body
                // Extra parameters if you want to pass to server

                System.out.println(""+Config.access_token);
                File fileObj = new File(uploadImagePath);
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
            System.out.println(">>>Upload profile pic :"+s);
            if (s != null) {
                p.dismiss();
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                p.dismiss();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                proceedAfterPermission();
            }
        }
    }

    private void checkCameraPermission() {
        if(ActivityCompat.checkSelfPermission(getActivity(), permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),permissionsRequired[0])){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.Permissionheader));
                builder.setMessage(getActivity().getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(getActivity().getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(getActivity(),permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton(getActivity().getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }  else if (permissionStatus.getBoolean(permissionsRequired[0],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.Permissionheader));
                builder.setMessage(getActivity().getResources().getString(R.string.cameraPermission));
                builder.setPositiveButton(getActivity().getResources().getString(R.string.grantpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getActivity().getBaseContext(), getActivity().getResources().getString(R.string.cameraSettingPermission), Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(getActivity().getResources().getString(R.string.cancelpermission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }else {
                //just request the permission
                ActivityCompat.requestPermissions(getActivity(),permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
        }else{
            proceedAfterPermission();

        }
    }


    class updateProfile extends AsyncTask<String, Void, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(getActivity());
            p.setCanceledOnTouchOutside(false);
            p.setMessage("Please wait..");
            p.show();
        }
        @Override
        protected String doInBackground(String... params) {
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            //  client.setConnectTimeout(120, TimeUnit.SECONDS); // connect timeout
            //client.setReadTimeout(120, TimeUnit.SECONDS);
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            System.out.println(">>> Update Profile params :"+ params[0]);
            RequestBody body = RequestBody.create(JSON, params[0]);
            Request request = new Request.Builder()
                    .url(Config.BASE_URL+"user/updateprofile")
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
            System.out.println(">>> Update Profile up result :" + s);
            p.dismiss();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String code = jsonObject.getString("code");
                    boolean is_success = jsonObject.getBoolean("is_success");
                    if(is_success == true){

                        Toast.makeText(getActivity(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        editor.putString("tabPosition","2");
                        editor.commit();
                        Intent intent = new Intent(getActivity(), ActivityDasshboard.class);
                        startActivity(intent);
                    }else{
                        String strMsg = jsonObject.getString("err_msg");
                        Toast.makeText(getActivity(),strMsg,Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Toast.makeText(ActivitySignUp.this,getResources().getString(R.string.noResponseMsg),Toast.LENGTH_SHORT).show();
            }
        }
    }



}
