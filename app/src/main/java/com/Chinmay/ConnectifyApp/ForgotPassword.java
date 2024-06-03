package com.Chinmay.ConnectifyApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class ForgotPassword extends AppCompatActivity {

    TextInputLayout forgotemail;
    FirebaseAuth auth;
    AppCompatButton forgotpasswordbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        forgotemail = findViewById(R.id.forgot_user_email);
        forgotpasswordbtn = findViewById(R.id.forgot_password_btn);
        auth = FirebaseAuth.getInstance();

        forgotpasswordbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = forgotemail.getEditText().getText().toString().trim();

                if (emailAddress.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_SHORT).show();
                } else {
                    auth.fetchSignInMethodsForEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        // Email exists, send password reset email
                                        auth.sendPasswordResetEmail(emailAddress)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getApplicationContext(), "Check Email To Reset Password.", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(getApplicationContext(), login.class));
                                                        } else {
                                                            Log.e("PasswordReset", "Error sending password reset email", task.getException());
                                                            Toast.makeText(getApplicationContext(), "Error sending password reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });

    }
}