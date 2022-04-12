package com.v.weasone.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.v.weasone.MainActivity;
import com.v.weasone.R;

public class Register extends AppCompatActivity {
    EditText regEmail, regPass, regConPass;
    TextView alreadyRegistered;
    Button createAccount;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    DatabaseReference dbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Initialize();
        dbref = FirebaseDatabase.getInstance().getReference();
        alreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccountEmailPassword();
            }
        });
    }

    private void createAccountEmailPassword() {
        String email = regEmail.getText().toString();
        String password = regPass.getText().toString();
        String confPassword = regConPass.getText().toString();
        int flag = 0;
        if(email.isEmpty()) {
            regEmail.setError("Field cannot be empty!");
            flag = 1;
        }
        if(password.isEmpty()){
            regPass.setError("Field cannot be empty!");
            flag = 1;
        }
        if(confPassword.isEmpty()){
            regConPass.setError("Field cannot be empty!");
            flag = 1;
        }
        if(!(confPassword.equals(password))){
            regConPass.setError("Password does not match!");
            flag = 1;
        }
        if(flag == 0){
            progressDialog.setTitle("Create New Account");
            progressDialog.setMessage("Please wait, while we are creating new Account...");
            progressDialog.show();

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        final String[] deviceToken = new String[1];
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                deviceToken[0] = task.getResult();
                            }
                        });
                        String currentUser = auth.getCurrentUser().getUid();
                        dbref.child("Users").child(currentUser).setValue("");
                        dbref.child("Users").child(currentUser).child("device_token").setValue(deviceToken[0]);
                        SendUserToMainActivity();
                        Toast.makeText(getBaseContext(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                }
                    else{
                        Toast.makeText(getBaseContext(), "Some Error Ocurred", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.cancel();
                    progressDialog.dismiss();
            }
            });
        }

    }

    private void SendUserToLoginActivity() {
        Intent i = new Intent(Register.this, Login.class);
        startActivity(i);
    }
    private void SendUserToMainActivity(){
        Intent i = new Intent(Register.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        Register.this.finish();
    }

    private void Initialize() {
        regEmail = findViewById(R.id.emailInputFieldRegisterUI);
        regPass = findViewById(R.id.passwordInputFieldRegisterUI);
        regConPass = findViewById(R.id.confirmPasswordInputFieldRegisterUI);
        alreadyRegistered  = findViewById(R.id.registeredLoginText);
        createAccount = findViewById(R.id.registerButton);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

    }

}