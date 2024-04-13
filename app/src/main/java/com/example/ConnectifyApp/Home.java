package com.example.ConnectifyApp;

    import static android.content.Context.TELEPHONY_SERVICE;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.AppCompatButton;
    import androidx.appcompat.widget.AppCompatRadioButton;
    import androidx.core.app.ActivityCompat;
    import androidx.core.view.GravityCompat;
    import androidx.drawerlayout.widget.DrawerLayout;
    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.app.AlertDialog;
    import android.app.Application;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.os.Bundle;
    import android.telephony.TelephonyManager;
    import android.util.Log;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.Toast;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.gms.tasks.Task;
    import com.google.android.material.navigation.NavigationView;
    import com.google.android.material.textfield.TextInputLayout;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.SetOptions;
    import com.permissionx.guolindev.PermissionX;
    import com.permissionx.guolindev.callback.ExplainReasonCallback;
    import com.permissionx.guolindev.callback.RequestCallback;
    import com.permissionx.guolindev.request.ExplainScope;
    import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
    import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
    import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
    import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
    import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
    import com.zegocloud.zimkit.services.ZIMKit;

    import java.util.Collections;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import im.zego.zim.enums.ZIMErrorCode;

public class Home extends AppCompatActivity {
    DrawerLayout drawerLayout;
    AppCompatRadioButton videoCall,voiceCall;
    TextInputLayout targetUsercallId;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String uid;
    String userProfileName,userPhoneNumber;

    ZegoSendCallInvitationButton zegoVoiceCallbtn , zegoVideoCallbtn;
    AppCompatButton startBtn;
    private static final int PERMISSION_REQUEST_READ_PHONE_NUMBERS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Toast.makeText(getApplicationContext(), "UID:" + mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();

        // For Left Side Drawer (Slide Bar)
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        startBtn = findViewById(R.id.startBtn);
        targetUsercallId = findViewById(R.id.user_callid);

        videoCall = findViewById(R.id.radioVideoCall);
        voiceCall = findViewById(R.id.radioVoiceCall);

        zegoVideoCallbtn = findViewById(R.id.zegovideocallbtn);
        zegoVoiceCallbtn = findViewById(R.id.zegovoicecallbtn);

        // Here I Disabled The ZegoSendCallInvitationButton Because When We Click On It Direcly Video Call Was Starting So I Disabled It
        zegoVideoCallbtn.setEnabled(false);
        zegoVoiceCallbtn.setEnabled(false);

        // Initiating A Zegocloud Chat
        initZegoCloudChat();
        //To Fetch The Mobile Number
        getPhoneNumber();
        //To Fetch The Mobile Number From Firebase
        fetchPhoneNumberFromFirebase();
        //To Fetch The Username Of User
        getUserName();


        // Permission For Display App Over Another Activity
        PermissionX.init(Home.this).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
                .onExplainRequestReason(new ExplainReasonCallback() {
                    @Override
                    public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                        String message = "We need your consent for the following permissions in order to use the offline call function properly";
                        scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny");
                    }
                }).request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                                         @NonNull List<String> deniedList)
                    {

                    }
                });


        // Navigation View For Left Side Drawer From Which We Can Select Option And Navigte To Specific Activity
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_logout) {
                    //Toast.makeText(getApplicationContext(),"Logout",Toast.LENGTH_SHORT).show();
                    logoutUser(navigationView);
                } else if (item.getItemId() == R.id.user_profile) {
                    startActivity(new Intent(getApplicationContext(), profile.class));
                    closeDrawer(navigationView);
                    //finish();
                }
                else if(item.getItemId()==R.id.chat)
                {
                    startActivity(new Intent(getApplicationContext(), Chat.class));
                    closeDrawer(navigationView);
                    //finish();
                }
                return false;
            }
        });


        // Start Button For Stating A Video Call
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
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


    // For Fetching The Phone Number From Firebase Which Is Added To Firebase
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
                                userPhoneNumber = phoneNumber;
                                Toast.makeText(getApplicationContext(),"Phone No:"+userPhoneNumber,Toast.LENGTH_SHORT).show();
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


    // Start Function Which Can Be Called Only When We Click On Start Button For Initiating Video Call
    private void start() {
        Toast.makeText(getApplicationContext(),"In Start Fuction",Toast.LENGTH_SHORT).show();

        String targetusercallid = targetUsercallId.getEditText().getText().toString();

        if (videoCall.isChecked()) {
            startService(userPhoneNumber, userProfileName);
            setVideoCall(targetusercallid);
            zegoVideoCallbtn.performClick();

        } else if (voiceCall.isChecked()) {
            startService(userPhoneNumber, userProfileName);
            setVoiceCall(targetusercallid);
            zegoVoiceCallbtn.performClick();

        } else {
            // Neither video nor voice call selected
            Toast.makeText(getApplicationContext(), "Please select a call type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetusercallid.isEmpty()) {
            // Target user ID is empty
            Toast.makeText(getApplicationContext(), "Please enter target user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start the call
        Toast.makeText(getApplicationContext(), "Starting call to " + targetusercallid, Toast.LENGTH_SHORT).show();
    }

    // It Is A Service That Will Be Start Before Initing Call To The User
    private void startService(String uid,String username) {
        Toast.makeText(getApplicationContext(),"In Start Service Function",Toast.LENGTH_SHORT).show();

        Toast.makeText(getApplicationContext(),"UID:"+uid+" Username:"+username,Toast.LENGTH_SHORT).show();

        Application application = getApplication(); // Android's application context
        long appID = 1125184998 ;   // yourAppID
        String appSign = "b49a0c57d03f55161f9251b5e359b73b800888fc1784d96d4fecf6b262c21adc";  // yourAppSign
        String userID = uid; // yourUserID, userID should only contain numbers, English characters, and '_'.
        String userName = username;   // yourUserName

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(getApplication(), appID, appSign, userID, userName,callInvitationConfig);
    }

    // For Starting Voice Call To User With His ID
    private void setVoiceCall(String targetUserId) {
        Toast.makeText(getApplicationContext(),"In Voice Call Fuction",Toast.LENGTH_SHORT).show();

        zegoVoiceCallbtn.setIsVideoCall(false);
        zegoVoiceCallbtn.setEnabled(false);
        zegoVoiceCallbtn.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        zegoVoiceCallbtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId)));
    }

    // For Starting Video Call To User With His ID
    private void setVideoCall(String targetUserId) {
        Toast.makeText(getApplicationContext(),"In Video Call Function",Toast.LENGTH_SHORT).show();

        zegoVideoCallbtn.setIsVideoCall(true);
        zegoVideoCallbtn.setResourceID("zego_uikit_call"); // Please fill in the resource ID name that has been configured in the ZEGOCLOUD's console here.
        zegoVideoCallbtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId)));
    }


    // Getting An Mobile Number From Device To Set User Call Id As Phone Number As Default
    private void getPhoneNumber() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean phoneNumberSaved = prefs.getBoolean("phoneNumberSaved", false);

        if (!phoneNumberSaved) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS}, PERMISSION_REQUEST_READ_PHONE_NUMBERS);
                    return;
                }
                userPhoneNumber = telephonyManager.getLine1Number();

                if (userPhoneNumber != null && !userPhoneNumber.isEmpty()){
                    Log.d("Phone Number", userPhoneNumber);
                    //Toast.makeText(getApplicationContext(),"Ph No:"+phoneNumber,Toast.LENGTH_SHORT).show();
                    savePhoneNoToFirebase(userPhoneNumber);
                    SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                    editor.putBoolean("phoneNumberSaved", true);
                    editor.apply();
                } else {
                    // Phone number not available
                    Log.e("Phone Number", "Phone number not available");
                }
            }
        }
    }

    // For Fetching The Username From Firebase
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
                                Toast.makeText(getApplicationContext(),"Username:"+userProfileName,Toast.LENGTH_SHORT).show();
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


    // Request Result For "GetPhoneNumber()"
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_PHONE_NUMBERS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch the phone number
                getPhoneNumber();
            } else {
                // Permission denied, handle accordingly
                Log.e("Permission", "READ_PHONE_NUMBERS permission denied");
            }
        }
    }

    // Saving Phone Number To The Firebase
    private void savePhoneNoToFirebase(String phNumber) {
        if (phNumber != null && !phNumber.isEmpty()) {
            uid = mAuth.getCurrentUser().getUid();

            // Check if phone number is already stored in Firestore
            db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String storedPhoneNumber = document.getString("usercallid");
                            if (storedPhoneNumber != null && storedPhoneNumber.equals(phNumber)) {
                                // Phone number already stored, no need to save again
                                Toast.makeText(getApplicationContext(), "Phone number already stored", Toast.LENGTH_SHORT).show();
                            } else {
                                // Save the phone number to Firestore
                                Map<String, Object> userPhNo = new HashMap<>();
                                userPhNo.put("usercallid", phNumber);

                                db.collection("users").document(uid)
                                        .set(userPhNo, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Phone No Added", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Error", "Error writing document", e);
                                                Toast.makeText(getApplicationContext(), "Error While Adding Phone No", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                }
            });
        } else {
            // Phone number not available
            Toast.makeText(getApplicationContext(), "Phone Number Not Available", Toast.LENGTH_SHORT).show();
        }
    }

    public void openDrawer(View view)
    {
        drawerLayout.open();
    }

    public void closeDrawer(View view)
    {
        drawerLayout.close();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(drawerToggle.onOptionsItemSelected(item))
        {
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
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

}