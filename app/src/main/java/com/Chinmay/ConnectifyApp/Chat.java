package com.Chinmay.ConnectifyApp;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zegocloud.zimkit.common.ZIMKitRouter;
import com.zegocloud.zimkit.common.enums.ZIMKitConversationType;
import com.zegocloud.zimkit.services.ZIMKit;

import java.util.Arrays;
import java.util.List;

import im.zego.zim.enums.ZIMErrorCode;

public class Chat extends AppCompatActivity {

    WebView webView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    String userProfileName,userProfileId,userProfileEmail,userProfileImage;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    FloatingActionButton floatingActionButton;
    private ImageView circleImageView;
    TextView drawerUserName,drawerUserEmail;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // For Left Side Drawer (Slide Bar)
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Getting the view of the Drawer from navigation view
        headerView = navigationView.getHeaderView(0);

        //Drawer Elements
        circleImageView = headerView.findViewById(R.id.drawer_image);
        drawerUserName = headerView.findViewById(R.id.drawerUserName);
        drawerUserEmail = headerView.findViewById(R.id.drawerUserEmail);


        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        webView = findViewById(R.id.webView);
        floatingActionButton = findViewById(R.id.floatingActionButton);

        // Load an image from databse to display
        fetchImage();

        // To Fetch number from firebase to diaplay it on profile page
        fetchPhoneNumberFromFirebase();

        // Load drawer data from databse to display
        loadDrawerData();

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
                else if(item.getItemId()==R.id.user_profile)
                {
                    startActivity(new Intent(getApplicationContext(), profile.class));
                    closeDrawer(navigationView);
                    //finish();
                }
                else if(item.getItemId()==R.id.about)
                {
                    startActivity(new Intent(getApplicationContext(), about_us.class));
                    closeDrawer(navigationView);
                    //finish();
                }
                return false;
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
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
                                    Glide.with(Chat.this).load(userProfileImage).into(circleImageView);
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
                                //Toast.makeText(getApplicationContext(),"Email:"+userProfileEmail,Toast.LENGTH_SHORT).show();
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

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(),floatingActionButton);
        popupMenu.getMenuInflater().inflate(R.menu.chat_options_menu,popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.newChat)
                {
                    showNewChatDialog();
                    return true;
                }
                if(item.getItemId()==R.id.createGroup)
                {
                    showNewGroupDialog();
                    return true;
                }
                if(item.getItemId()==R.id.joinGroup)
                {
                    showJoinGroupDialog();
                    return true;
                }
                if(item.getItemId()==R.id.chatLogout)
                {
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void showJoinGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
        builder.setTitle("Join New Group");

        EditText groupId = new EditText(Chat.this);
        groupId.setHint("Group Id");
        builder.setView(groupId);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                joinGroupChat(groupId.getText().toString());
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface alertDialog, int which) {
                alertDialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void showNewGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
        builder.setTitle("New Group");

        EditText groupId = new EditText(Chat.this);
        groupId.setHint("Group Id");

        EditText userIds = new EditText(Chat.this);
        userIds.setHint("User Id");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(groupId);
        linearLayout.addView(userIds);
        builder.setView(linearLayout);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                List<String> ids = Arrays.asList(userIds.getText().toString().split(","));
                createGroupChat(ids,groupId.getText().toString());
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface alertDialog, int which) {
                alertDialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showNewChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
        builder.setTitle("New Chat");

        EditText targetUserId = new EditText(Chat.this);
        targetUserId.setHint("Target User Id");
        builder.setView(targetUserId);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSingleChat(targetUserId.getText().toString());
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface alertDialog, int which) {
                alertDialog.dismiss();
            }
        });

        builder.create().show();
    }

    public void joinGroupChat(String groupId) {
        ZIMKit.joinGroup(groupId, (groupInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                // Enter the group chat page after joining the group chat successfully.
                ZIMKitRouter.toMessageActivity(this, groupInfo.getId(),ZIMKitConversationType.ZIMKitConversationTypeGroup);
            } else {
                // Implement the logic for the prompt window based on the returned error info when failing to join the group chat.
            }
        });
    }
    private void startSingleChat(String targetUserId){
        ZIMKitRouter.toMessageActivity(Chat.this, targetUserId, ZIMKitConversationType.ZIMKitConversationTypePeer);
    }

    public void createGroupChat(List<String> ids, String groupName) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ZIMKit.createGroup(groupName, ids, (groupInfo, inviteUserErrors, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                if (!inviteUserErrors.isEmpty()) {
                    // Implement the logic for the prompt window based on your business logic when there is a non-existing user ID in the group.
                } else {
                    // Directly enter the chat page when the group chat is created successfully.
                    ZIMKitRouter.toMessageActivity(this, groupInfo.getId(),ZIMKitConversationType.ZIMKitConversationTypeGroup);
                }
            } else {
                // Implement the logic for the prompt window based on the returned error info when failing to create a group chat.
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
                                //Toast.makeText(getApplicationContext(),"Phone No:"+userProfileId,Toast.LENGTH_SHORT).show();
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
                                userProfileName = username;
                                //Toast.makeText(getApplicationContext(),"Username:"+userProfileName,Toast.LENGTH_SHORT).show();
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
