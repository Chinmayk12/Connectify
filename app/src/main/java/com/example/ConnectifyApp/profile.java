
package com.example.ConnectifyApp;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
public class profile extends AppCompatActivity {
    ImageButton cameraBtn;
    AppCompatButton updateProfilebtn;
    TextInputLayout profileUsername, userCallId;
    CircleImageView circleImageView;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    private FirebaseAuth mAuth;
    StorageReference storageReference;
    String imageId;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        circleImageView = findViewById(R.id.profile_image);
        cameraBtn = findViewById(R.id.camerabtn);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        profileUsername = (TextInputLayout) findViewById(R.id.user_name);
        userCallId = (TextInputLayout) findViewById(R.id.user_callid);
        updateProfilebtn = (AppCompatButton) findViewById(R.id.update_profile_btn);

        //Setting name to the ProfileName
        loadUserName();

        // Load an image from databse to display
        loadImage();

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
                } else if (item.getItemId() == R.id.user_profile) {
                    startActivity(new Intent(getApplicationContext(), profile.class));
                }
                else if (item.getItemId()==R.id.home)
                {
                    startActivity(new Intent(getApplicationContext(), Home.class));
                    closeDrawer(navigationView);
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

    private void loadUserName() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                Log.d("UserName", username);
                                profileUsername.getEditText().setText(username);
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
        String profileusername = profileUsername.getEditText().getText().toString();
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
        circleImageView.setImageURI(uri);

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

    // Working
    private void loadImage() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                // Use an image loading library like Glide or Picasso to load the image into your circleImageView
                                Glide.with(profile.this).load(imageUrl).into(circleImageView);
                            } else {
                                // Set a placeholder image if the imageUrl is not available
                                circleImageView.setImageResource(R.drawable.profile_image_logo);
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
