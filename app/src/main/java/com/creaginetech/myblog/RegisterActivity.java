package com.creaginetech.myblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtRegEmail, edtRegPassword, edtRegPasswordConfirm;
    private Button btnRegister, btnRegisterLogin;
    private ProgressBar progressBarRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerWidgets();

        btnRegisterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String regEmail = edtRegEmail.getText().toString();
                String regPass = edtRegPassword.getText().toString();
                String regPaassConfirm = edtRegPasswordConfirm.getText().toString();

                if (!TextUtils.isEmpty(regEmail) && !TextUtils.isEmpty(regPass) && !TextUtils.isEmpty(regPaassConfirm)){
                    //if password equals with confirm password field
                    if (regPass.equals(regPaassConfirm)){

                        progressBarRegister.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(regEmail,regPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){

                                    Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();


                                }else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error : " +errorMessage, Toast.LENGTH_LONG).show();
                                }
                                progressBarRegister.setVisibility(View.INVISIBLE);
                            }
                        });

                    }else {

                        Toast.makeText(RegisterActivity.this, "Password and Confirm Password doesn't match !", Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });

    }

    private void registerWidgets() {

        edtRegEmail = findViewById(R.id.editText_register_email);
        edtRegPassword = findViewById(R.id.editText_register_password);
        edtRegPasswordConfirm = findViewById(R.id.editText_register_confirm_password);
        btnRegister = findViewById(R.id.button_register);
        btnRegisterLogin = findViewById(R.id.button_register_login);
        progressBarRegister = findViewById(R.id.progressBar_register);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            sendToMain();


        }
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

    }
}
