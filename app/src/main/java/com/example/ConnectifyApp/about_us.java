package com.example.ConnectifyApp;

import static com.zegocloud.zimkit.services.ZIMKit.connectUser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zegocloud.zimkit.services.ZIMKit;

import im.zego.zim.enums.ZIMErrorCode;

public class about_us extends AppCompatActivity {

    WebView webView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    String userProfileId,profileUserName,userProfileImage;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        webView = findViewById(R.id.webView);

        // Initiating A Zegocloud Chat
        initZegoCloudChat();

        // Load an image from databse to display
        fetchImage();

        // To Fetch number from firebase to diaplay it on profile page
        fetchPhoneNumberFromFirebase();

        //To Fetch The Username Of User
        getUserName();


        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://chinmayk12.github.io/chinmaykarodpati/");



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_logout) {
                    //Toast.makeText(getApplicationContext(),"Logout",Toast.LENGTH_SHORT).show();
                    logoutUser(navigationView);
                }
                else if (item.getItemId()==R.id.home)   // Video Call
                {
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();
                    closeDrawer(navigationView);
                }
                else if(item.getItemId()==R.id.chat)
                {
                    connectUser(userProfileId, profileUserName,userProfileImage);
                    closeDrawer(navigationView);
                    //finish();
                }
                else if(item.getItemId()==R.id.user_profile)
                {
                    startActivity(new Intent(getApplicationContext(), profile.class));
                    closeDrawer(navigationView);
                    //finish();
                }
                return false;
            }
        });
    }

    private void initZegoCloudChat() {
        Long appId = (long) 263772551;    // The AppID you get from ZEGOCLOUD Admin Console.
        String appSign = "4fde8262fa24e2a8e59e9a0ea37c6e61d725fd42286f67857d677af579d898b1";    // The App Sign you get from ZEGOCLOUD Admin Console.
        ZIMKit.initWith(getApplication(),appId,appSign);
        // Online notification for the initialization (use the following code if this is needed).
        ZIMKit.initNotifications();
    }

    public void connectUser(String userId, String userName,String userAvatar) {
        // Logs in.
        ZIMKit.connectUser(userId,userName,userAvatar, errorInfo -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                // Operation after successful login. You will be redirected to other modules only after successful login. In this sample code, you will be redirected to the conversation module.
                toConversationActivity();
            } else {

            }
        });
    }

    // Integrate the conversation list into your Activity as a Fragment
    private void toConversationActivity() {
        // Redirect to the conversation list (Activity) you created.
        Intent intent = new Intent(this,Chat.class);
        startActivity(intent);
    }

    private void getUserName() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                Log.d("UserName", username);
                                profileUserName = username;

                                //Toast.makeText(getApplicationContext(),"Username:"+profileUserName,Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("Username ", "Username number not found");
                            }
                        } else {
                            Log.e("Username", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Phone Number", "Error fetching document", e);
                    }
                });
    }

    private void fetchImage() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            userProfileImage = documentSnapshot.getString("imageUrl");
                            if (userProfileImage != null && !userProfileImage.isEmpty()) {
                                Log.d("User Avatar", userProfileImage);
                            } else {
                                Log.e("User Avatar", "User avatar not found");
                            }
                        } else {
                            Log.e("User Avatar", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("User Avatar", "Error fetching document", e);
                    }
                });

    }

    private void fetchPhoneNumberFromFirebase() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String phoneNumber = documentSnapshot.getString("usercallid");
                            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                Log.d("Phone Number", phoneNumber);
                                userProfileId = phoneNumber;
                            } else {
                                Log.e("Phone Number", "Phone number not found");
                            }
                        } else {
                            Log.e("Phone Number", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Phone Number", "Error fetching document", e);
                    }
                });
    }

    public void openDrawer(View view) {
        drawerLayout.open();
    }
    public void closeDrawer(View view)
    {
        drawerLayout.close();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logoutUser(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Sign out the current authenticated user from Firebase
                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(getApplicationContext(), login.class));
                finishAffinity();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface alertDialog, int which) {
                // User clicked No, do nothing
                alertDialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

}