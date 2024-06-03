
package com.Chinmay.ConnectifyApp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.zimkit.services.ZIMKit;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import im.zego.zim.enums.ZIMErrorCode;

public class profile extends AppCompatActivity {
    ImageButton cameraBtn;
    AppCompatButton updateProfilebtn;
    TextInputLayout etprofileUsername, userCallId;
    CircleImageView profilecircleImageView;
    ImageView drawercircleImageView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    String imageId;
    FirebaseFirestore db;
    private View headerView;
    String userProfileId,profileUserName,userProfileEmail,userProfileImage;
    TextView drawerUserName,drawerUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        profilecircleImageView = findViewById(R.id.profile_image);
        cameraBtn = findViewById(R.id.camerabtn);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Getting the view of the Drawer from navigation view
        headerView = navigationView.getHeaderView(0);

        //Drawer Elements
        drawercircleImageView = headerView.findViewById(R.id.drawer_image);
        drawerUserName = headerView.findViewById(R.id.drawerUserName);
        drawerUserEmail = headerView.findViewById(R.id.drawerUserEmail);

        etprofileUsername = (TextInputLayout) findViewById(R.id.user_name);
        userCallId = (TextInputLayout) findViewById(R.id.user_callid);
        updateProfilebtn = (AppCompatButton) findViewById(R.id.update_profile_btn);

        // Initiating A Zegocloud Chat
        initZegoCloudChat();

        //Setting name to the ProfileName
        getUserName();

        // Load drawer data from databse to display
        loadDrawerData();

        // To Fetch number from firebase to diaplay it on profile page
        fetchPhoneNumberFromFirebase();

        updateProfilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserData();
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_logout) {
                    //Toast.makeText(getApplicationContext(),"Logout",Toast.LENGTH_SHORT).show();
                    logoutUser(navigationView);
                }
                else if (item.getItemId()==R.id.home)
                {
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    finish();
                    closeDrawer(navigationView);
                }
                else if(item.getItemId()==R.id.chat)
                {
                    connectUser(userProfileId, profileUserName,userProfileImage);
                    startActivity(new Intent(getApplicationContext(), Chat.class));
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

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(profile.this)
                        .crop()                            //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
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
                                etprofileUsername.getEditText().setText(username);
                                drawerUserName.setText(profileUserName);
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

    public void initZegoCloudChat()
    {
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
                                userCallId.getEditText().setText(phoneNumber);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        storeImageInFirestore(data);

        // Delete the old image if it exists
        if (storageReference != null) {
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("ProfileActivity", "Old image deleted successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("ProfileActivity", "Failed to delete old image: " + e.getMessage());
                }
            });
        }
    }

    private void updateUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        String profileusername = etprofileUsername.getEditText().getText().toString();
        String usercallid = userCallId.getEditText().getText().toString();

        if (profileusername.isEmpty() || usercallid.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", profileusername);
        userData.put("usercallid", usercallid);

        db.collection("users").document(uid)
                .update(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Data Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", "Error updating document", e);
                        Toast.makeText(getApplicationContext(), "Error updating data", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void storeImageInFirestore(Intent data) {

        Uri uri = data.getData();
        profilecircleImageView.setImageURI(uri);

        ProgressDialog progressDialog = new ProgressDialog(profile.this);
        progressDialog.setTitle("File Uploader");
        progressDialog.show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        DatabaseReference myRef = database.getReference();

        myRef = FirebaseDatabase.getInstance().getReference();
        imageId = myRef.push().getKey();


        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference uploader = firebaseStorage.getReference().child(mAuth.getCurrentUser().getUid() + "/" + imageId);

        uploader.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();

                        Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();

                        // Get the download URL
                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();

                                // Storing image url in FireStoreDatabase
                                storeImageUrlInFirestore(imageUrl);
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + (int) percent + " %");
                    }
                });

    }

    // Done Working
    private void storeImageUrlInFirestore(String imageUrl) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        myRef = FirebaseDatabase.getInstance().getReference();
        String imageId = myRef.push().getKey();

        // Assuming you have a "users" collection in Firestore
        // Update the path according to your Firestore structure
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(mAuth.getCurrentUser().getUid())
                .update("imageUrl", imageUrl)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Image URL stored in Firestore successfully
                        Toast.makeText(getApplicationContext(), "Image Uploaded to FireStore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to store image URL in Firestore
                        Toast.makeText(getApplicationContext(), "Failed to Uploade An Image To FireStore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Load Image Working
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
                                // Use an image loading library like Glide or Picasso to load the image into your circleImageView
                                Glide.with(profile.this).load(userProfileImage).into(profilecircleImageView);
                                Glide.with(profile.this).load(userProfileImage).into(drawercircleImageView);
                            } else {
                                // Set a placeholder image if the imageUrl is not available
                                profilecircleImageView.setImageResource(R.drawable.profile_image_logo);
                                drawercircleImageView.setImageResource(R.drawable.profile_image_logo);
                            }
                        } else {
                            Log.d("ProfileActivity", "No such document");
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