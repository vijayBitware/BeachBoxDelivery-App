package com.beachboxdeliveryapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.beachboxdeliveryapp.R;

/**
 * Created by bitware on 12/7/17.
 */

public class Test extends AppCompatActivity {

    EditText etCardNo,etmonth,etCvv,etZipCode;
    public int pos=0;
    TextView tvSaveCard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acrtivity_add_paymentcard);

        init();
        etCardNo.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                if(etCardNo.getText().length()==4 && pos!=5)
                {   etCardNo.setText(etCardNo.getText().toString()+"-");
                    etCardNo.setSelection(5);
                }else if (etCardNo.getText().length()==9 && pos!=10){
                    etCardNo.setText(etCardNo.getText().toString()+"-");
                    etCardNo.setSelection(10);
                }
                else if (etCardNo.getText().length()==14 && pos!=15){
                    etCardNo.setText(etCardNo.getText().toString()+"-");
                    etCardNo.setSelection(15);
                }

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub
                pos=etCardNo.getText().length();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

            }
        });

        etmonth.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
                if(etmonth.getText().length()==2 && pos!=3) {
                    etmonth.setText(etmonth.getText().toString() + "/");
                    etmonth.setSelection(3);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub
                pos=etmonth.getText().length();
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

            }
        });

        tvSaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNo = etCardNo.getText().toString();
                String s = cardNo.replace("-","").trim();
                System.out.println("Card no >>> " +s);
                System.out.println("Expiry month >>> " +etmonth.getText().toString());
                System.out.println("CVV >>> " +etCvv.getText().toString());
                System.out.println("Zipcode >>> " +etZipCode.getText().toString());

            }
        });

    }

    private void init() {
        etCardNo = (EditText) findViewById(R.id.etCardNo);
        etmonth = (EditText) findViewById(R.id.etmonth);
        etCvv= (EditText) findViewById(R.id.etCvv);
        etZipCode = (EditText) findViewById(R.id.etZipCode);
        tvSaveCard= (TextView) findViewById(R.id.tvSaveCard);
    }
}
