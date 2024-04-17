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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
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
    String userProfileId,profileUserName,userProfileImage,userProfileEmail;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    private ImageView circleImageView;
    TextView drawerUserName,drawerUserEmail;
    private View headerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        webView = findViewById(R.id.webView);

        // Getting the view of the Drawer from navigation view
        headerView = navigationView.getHeaderView(0);

        //Drawer Elements
        circleImageView = headerView.findViewById(R.id.drawer_image);
        drawerUserName = headerView.findViewById(R.id.drawerUserName);
        drawerUserEmail = headerView.findViewById(R.id.drawerUserEmail);
        // Initiating A Zegocloud Chat
        initZegoCloudChat();

        // Load an image from databse to display
        fetchImage();

        // To Fetch number from firebase to diaplay it on profile page
        fetchPhoneNumberFromFirebase();

        // Load drawer data from databse to display
        loadDrawerData();


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

    private void loadDrawerData() {
        //Load an image from database
        loadImage();

        //load username
        getUserName();

        //load useremail
        getUserEmail();

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

    private void loadImage() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            userProfileImage = documentSnapshot.getString("imageUrl");
                            if (userProfileImage != null && !userProfileImage.isEmpty()) {
                                Log.d("User Avatar", userProfileImage);
                                if (circleImageView != null) {
                                    Glide.with(about_us.this).load(userProfileImage).into(circleImageView);
                                } else {
                                    Log.e("User Avatar", "CircleImageView is null");
                                }
                            } else {
                                Log.e("User Avatar", "User avatar URL is null or empty");
                            }
                        } else {
                            Log.e("User Avatar", "Document does not exist");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ProfileActivity", "Error getting document", e);
                    }
                });
    }

    private void getUserEmail() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String email = documentSnapshot.getString("email");
                            if (email != null && !email.isEmpty()) {
                                Log.d("UserName", email);
                                userProfileEmail = email;
                                drawerUserEmail.setText(userProfileEmail);
                                Toast.makeText(getApplicationContext(),"Email:"+userProfileEmail,Toast.LENGTH_SHORT).show();
                            } else {
                                drawerUserEmail.setText("Email: Email Not Found");
                                Log.e("Email ", "Email not found");
                            }
                        } else {
                            Log.e("Email", "Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Email Error", "Error fetching document", e);
                    }
                });
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