package com.v.weasone.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.v.weasone.MainActivity;
import com.v.weasone.R;

import java.util.Objects;

public class Login extends AppCompatActivity {
    TextView register, forgotPassword;
    Button phoneLogin;
    Button LogIn;
    EditText email,password;
    DatabaseReference UserRef;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Initialize();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToPhoneLoginActivity();
            }
        });
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserLogIn();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLinktoMail();
            }
        });
    }

    private void Initialize() {
        register = findViewById(R.id.signUpText);
        phoneLogin = findViewById(R.id.loginPhoneButton);
        email=findViewById(R.id.emailInputField);
        password=findViewById(R.id.passwordInputField);
        LogIn=findViewById(R.id.loginButton);
        forgotPassword=findViewById(R.id.forgotPasswordField);
        auth=FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        UserRef = FirebaseDatabase.getInstance().getReference();
    }
    private void SendUserToRegisterActivity() {
        Intent i = new Intent(Login.this, Register.class);
        startActivity(i);
    }
    private void SendUserToPhoneLoginActivity() {
        Intent i = new Intent(Login.this, PhoneLogin.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Login.this.finish();
    }

    private void sendLinktoMail() {
        if (email.getText().toString().matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$") && email.getText().toString().length()>8)
        {
            AlertDialog.Builder passwordreset=new AlertDialog.Builder(this);
            passwordreset.setTitle("Reset Password ?");
            passwordreset.setMessage("Press Yes to receive the reset link");
            passwordreset.setPositiveButton("YES",(dialogInterface, i) ->
            {
                String resetEmail=email.getText().toString();
                auth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Reset Email Link has been send to your emailId", Toast.LENGTH_SHORT).show();
                    }
                });
            });
            passwordreset.setNegativeButton("NO",(dialogInterface, i) -> {});
            passwordreset.create().show();
        }
        else
        {
            email.setError("please Enter a valid Email");
        }


    }

    private void AllowUserLogIn() {
        String userEmail=email.getText().toString();
        String userPass=password.getText().toString();
        if(TextUtils.isEmpty(userEmail))
        {
            email.setError("please enter email id");

        }
        if(TextUtils.isEmpty(userPass))
        {
            password.setError("please enter password");

        }
        if(TextUtils.isEmpty(userEmail) && TextUtils.isEmpty(userPass))
        {
            email.setError("please enter email id");
            password.setError("please enter password");

        }
        if(!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)) {
            progressDialog.setTitle("Signing In");
            progressDialog.setMessage("Please wait , while we are logging into your account ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            auth.signInWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();
                        String currentUserId=auth.getCurrentUser().getUid();
                        UserRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SendUserToMainActivity();
                                Toast.makeText(getApplicationContext(), "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                    else
                    {
                        String message=task.getException().getLocalizedMessage();
                        Toast.makeText(getApplicationContext(), "Error : "+message, Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.cancel();
                    progressDialog.dismiss();
                }
            });
        }
    }
    private void SendUserToMainActivity() {
        Intent intent =new Intent(Login.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}