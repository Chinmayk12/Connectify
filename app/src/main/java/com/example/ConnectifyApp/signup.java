package com.example.ConnectifyApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class signup extends AppCompatActivity {
    FirebaseFirestore db;
    TextInputLayout name, email, password, repassword;
    AppCompatButton signupbtn;
    TextView login;
    private FirebaseAuth mAuth;

    String emailtxt, passwordtxt, nametxt, repasswordtxt;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        signupbtn = findViewById(R.id.signup_btn);
        login = findViewById(R.id.aldlogin);

        name = findViewById(R.id.signup_name);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        repassword = findViewById(R.id.signup_RepasswordLayout);

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailtxt = email.getEditText().getText().toString();
                passwordtxt = password.getEditText().getText().toString();
                repasswordtxt = repassword.getEditText().getText().toString();
                nametxt = name.getEditText().getText().toString();

                if (emailtxt.isEmpty() || passwordtxt.isEmpty() || repasswordtxt.isEmpty() || nametxt.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!passwordtxt.equals(repasswordtxt)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    createUserWithEmailAndPassword(emailtxt, passwordtxt);
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), com.example.ConnectifyApp.login.class));
                finishAffinity();
            }
        });
    }

    private void createUserWithEmailAndPassword(String email,String password) {
        Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nametxt)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Profile updated successfully
                                            } else {
                                                // Failed to update profile
                                            }
                                        }
                                    });

                            Toast.makeText(getApplicationContext(), "User Registered Successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(signup.this, login.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "An error occurred: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("Signup Error", "Error creating user", task.getException());
                        }
                    }
                });

    }
}