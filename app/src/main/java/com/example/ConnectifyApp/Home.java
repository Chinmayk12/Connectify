package com.example.ConnectifyApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Intent intent =getIntent();
        Toast.makeText(getApplicationContext(),"UID:"+intent.getStringExtra("uid"),Toast.LENGTH_SHORT).show();
    }
}