package com.v.weasone.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.v.weasone.MainActivity;
import com.v.weasone.R;

import java.util.concurrent.TimeUnit;

public class PhoneLogin extends AppCompatActivity {
    EditText phoneNumber, code;
    Button verifyCode, sendCode;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    String verificationID;
    PhoneAuthProvider.ForceResendingToken token;
    TextInputLayout phoneNo, codeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        Initialize();
        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNum = phoneNumber.getText().toString();
                if(phoneNum.isEmpty()){
                    phoneNumber.setError("Enter Phone Number!");
                }
                else{
                    progressDialog.setTitle("Mobile Login");
                    progressDialog.setMessage("Sending Code, Please wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber("+91"+phoneNum)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(PhoneLogin.this)
                            .setCallbacks(callbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
                    auth.setLanguageCode("en");


                }

            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.cancel();
                phoneNo.setVisibility(View.VISIBLE);
                sendCode.setVisibility(View.VISIBLE);
                codeLayout.setVisibility(View.GONE);
                verifyCode.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Invalid Phone Number , please enter correct phone number", Toast.LENGTH_SHORT).show();
                if(e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Toast.makeText(getApplicationContext(), "Invalid Request : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                if(e instanceof FirebaseTooManyRequestsException)
                {
                    Toast.makeText(getApplicationContext(), " Your sms limit has been expired ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken Token) {
                progressDialog.cancel();
                verificationID=s;
                token=Token;
                Toast.makeText(getApplicationContext(), "Code Sent", Toast.LENGTH_SHORT).show();
                sendCode.setVisibility(View.GONE);
                codeLayout.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
            }
        };
        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCode.setVisibility(View.GONE);
                codeLayout.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
                String codes=code.getText().toString();
                if(TextUtils.isEmpty(codes))
                {
                    code.setError("please enter verification code");
                }
                else
                {
                    progressDialog.setTitle("Verification Code");
                    progressDialog.setMessage("Please wait , while we are verifying you code ....");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationID,codes);
                    signInWithPhoneCredentials(credential);
                }
            }
        });


    }

    private void Initialize() {
        phoneNumber = findViewById(R.id.mobileNoInputField);
        code  = findViewById(R.id.codeInputField);
        sendCode = findViewById(R.id.sendCodeButton);
        verifyCode = findViewById(R.id.codeVerifyButton);
        codeLayout = findViewById(R.id.codeInputLayout);
        phoneNo = findViewById(R.id.mobileNoInputLayout);
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        codeLayout.setVisibility(View.GONE);
        verifyCode.setVisibility(View.GONE);
    }
    public void signInWithPhoneCredentials(PhoneAuthCredential phoneAuthCredential)
    {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
                    String currentUserId=auth.getCurrentUser().getUid();
                    DatabaseReference UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
                    UserRef.child(currentUserId).child("device_token").setValue(deviceToken);
                    Toast.makeText(getApplicationContext(), "you are successfully logged In", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }
                else
                {
                    String message =task.getException().getMessage();
                    Toast.makeText(getApplicationContext(), "Error : " +  message, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                progressDialog.cancel();

            }
        });
    }
    private void SendUserToMainActivity() {
        Intent intent =new Intent(PhoneLogin.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}