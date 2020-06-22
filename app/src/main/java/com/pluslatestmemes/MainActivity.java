package com.pluslatestmemes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rilixtech.CountryCodePicker;

public class MainActivity extends AppCompatActivity {
    private Button mProceed;
    private CountryCodePicker mCountryCodePicker;
    private EditText mPhone;
    private String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProceed = findViewById(R.id.btn_proceed_signup);
        mPhone = findViewById(R.id.edt_phone_signup);
        mCountryCodePicker = findViewById(R.id.ccp);

        code = mCountryCodePicker.getSelectedCountryCode().toString();

        mProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendingVerificationCode();
            }
        });
    }


    private void sendingVerificationCode(){
        final String mobile = mPhone.getText().toString().trim();

        if (mobile.isEmpty() || mobile.length() < 9 || mobile.length() > 9) {
            mPhone.setError("Enter a valid mobile number");
            mPhone.requestFocus();
            return;
        } else {

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Account Creation...");
            builder.setMessage("Is this the number you want to create account with\n"+"+"+code + mobile + "?");
// Add the buttons
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    Intent intent = new Intent(MainActivity.this, VerificationCode.class);
                    intent.putExtra("mobile", "+"+code+mobile);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
