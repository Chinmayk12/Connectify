package com.example.ConnectifyApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {

    TextView signup;
    TextInputLayout email, password;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    AppCompatButton loginbtn;

    String emailtxt, passwordtxt;
    CardView googlelogincard,facebooklogincard;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 100;
    private  CallbackManager callbackManager;
    String uid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loginbtn = findViewById(R.id.login_btn);
        googlelogincard = findViewById(R.id.googlelogincard);
        facebooklogincard = findViewById(R.id.facebooklogincard);

        signup = findViewById(R.id.noAccountSignUp);
        email = findViewById(R.id.user_email);
        password = findViewById(R.id.user_password);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailLogin();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), com.example.ConnectifyApp.signup.class));
            }
        });

        googlelogincard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googlelogin();
            }
        });

        facebooklogincard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookLogin();
            }
        });
    }


    private void googlelogin() {

        //Toast.makeText(getApplicationContext(),"Google Login",Toast.LENGTH_SHORT).show();

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = getGoogleSignInRequest();

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(result.getPendingIntent().getIntentSender(), REQ_ONE_TAP, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("Error:", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error:", e.getLocalizedMessage());
                    }
                });
    }

    private BeginSignInRequest getGoogleSignInRequest() {
        return BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId("228353396534-gb0c6htdtgj1br0ctmudesuk2mhsh3e1.apps.googleusercontent.com")
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(true)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            handleGoogleSignInResult(data);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleGoogleSignInResult(Intent data) {
        try {
            //Toast.makeText(getApplicationContext(),"In Google Handle Method",Toast.LENGTH_SHORT).show();
            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);

            mAuth.signInWithCredential(GoogleAuthProvider.getCredential(credential.getGoogleIdToken(), null))
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                saveUserDataToFirebase();
                            } else {
                                Log.w("Google SignIn", "signInWithCredential:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (ApiException e) {
            Log.e("Google SignIn", "API exception: " + e.getStatusCode());
        }
    }

    public void emailLogin() {

        String emailtxt = email.getEditText().getText().toString().trim();
        String passwordtxt = password.getEditText().getText().toString().trim();

        if (emailtxt.isEmpty() || passwordtxt.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            mAuth.signInWithEmailAndPassword(emailtxt, passwordtxt)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                saveUserDataToFirebase();
                                startHomeActivity();
                            } else {
                                email.getEditText().setText("");
                                password.getEditText().setText("");
                                Toast.makeText(getApplicationContext(), "Invalid Email Or Password.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void facebookLogin() {
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Facebook Exception", exception.toString());
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }


    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveUserDataToFirebase();
                            startHomeActivity();
                        } else {
                            Log.w("Facebook SignIn", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDataToFirebase() {
        //Toast.makeText(getApplicationContext(),"Login",Toast.LENGTH_SHORT).show();
        String username, email;

        uid = mAuth.getCurrentUser().getUid();
        username = user.getDisplayName();
        email = mAuth.getCurrentUser().getEmail();

        Map<String, Object> data = new HashMap<>();
        data.put("name", username.toString().trim());
        data.put("email", email.toString().trim());

        db.collection("users").document(uid)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startHomeActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error writing document", e);
                    }
                });
    }

    private void startHomeActivity()
    {
        Intent homeIntent = new Intent(getApplicationContext(), Home.class);
        homeIntent.putExtra("uid", uid);
        startActivity(homeIntent);
    }
}