package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        FirebaseApp.initializeApp(this);

        Button createAccount = findViewById(R.id.create_account_btn);
        createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, regActivity.class);
            startActivity(intent);
        });
    }
}
