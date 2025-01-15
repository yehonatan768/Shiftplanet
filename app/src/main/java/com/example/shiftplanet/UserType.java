package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserType extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_type);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void ManagerLogin(View v) {
        Intent intent = new Intent(UserType.this, ManagersLogin.class);  // Create an Intent to navigate to the Registration activity
        startActivity(intent);  // Start the Registration activity
    }
    public void EmployeeLogin(View v) {
        Intent intent = new Intent(UserType.this, EmployeesLogin.class);  // Create an Intent to navigate to the Registration activity
        startActivity(intent);  // Start the ManagerHomePage activity
    }
}