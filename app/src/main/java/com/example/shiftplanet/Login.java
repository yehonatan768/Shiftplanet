package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Login extends AppCompatActivity {

    // Declare EditText fields for email and password input
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;  // Firebase Authentication instance
    private FirebaseFirestore db;  // Firebase Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);  // Set the layout for the login activity

        // Initialize EditText fields for email and password
        emailEditText = findViewById(R.id.email_input);
        passwordEditText = findViewById(R.id.password_input);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the login button action
        findViewById(R.id.login_btn).setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();  // Get email input
        String password = passwordEditText.getText().toString().trim();  // Get password input

        // Check if email or password is empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to sign in the user with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();  // Get the current logged-in user
                        if (user != null) {
                            // Retrieve user data from Firestore
                            db.collection("users")
                                    .document(user.getUid())  // Get the document corresponding to the user
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();  // Get the document data
                                            if (document.exists()) {
                                                // Retrieve the user type from the document
                                                String userType = document.getString("user_type");

                                                // Check if the user type exists and is valid

                                                if (userType != null) {
                                                    // Navigate to the appropriate home page based on user type (case-insensitive comparison)
                                                    Intent intent;
                                                    System.out.println("lalala1");
                                                    if (userType.equalsIgnoreCase("Manager")) {
                                                        System.out.println("lalala2");
                                                        intent = new Intent(Login.this, ManagerHomePage.class);  // Manager user
                                                    }
                                                    else if (userType.equalsIgnoreCase("Employee")) {
                                                        System.out.println("lalala3");
                                                        intent = new Intent(Login.this, EmployeeHomePage.class);  // Employee user
                                                    }
                                                    else {
                                                        System.out.println("lalala4");
                                                        Toast.makeText(Login.this, "Unknown user type", Toast.LENGTH_SHORT).show();
                                                        return;  // If the user type is unknown, show an error
                                                    }


                                                    System.out.println("lalala5");
                                                    startActivity(intent);  // Start the appropriate home page activity
                                                    finish();  // Close the current login activity
                                                } else {
                                                    // Handle missing user type in Firestore
                                                    Log.w("LoginActivity", "User type is missing in Firestore");
                                                    Toast.makeText(Login.this, "User data incomplete. Please contact admin.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(Login.this, "User data not found in Firestore", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(Login.this, "Failed to retrieve user data from Firestore", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to navigate to the Registration activity when the registration button is clicked
    public void register(View v) {
        Intent intent = new Intent(Login.this, Registration.class);  // Create an Intent to navigate to the Registration activity
        startActivity(intent);  // Start the Registration activity
    }
    public void forgotPassword(View v) {
        Intent intent = new Intent(Login.this, ForgotPassword.class);  // Create an Intent to navigate to the Registration activity
        startActivity(intent);  // Start the Registration activity
    }
}
