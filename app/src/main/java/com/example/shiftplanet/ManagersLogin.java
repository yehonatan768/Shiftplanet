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

public class ManagersLogin extends AppCompatActivity {

    // Declare EditText fields for email and password input
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;  // Firebase Authentication instance
    private FirebaseFirestore db;  // Firebase Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managers_login);  // Set the layout for the login activity

        // Initialize EditText fields for email and password
        emailEditText = findViewById(R.id.email_input1);
        passwordEditText = findViewById(R.id.password_input1);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the login button action
        findViewById(R.id.login_btn1).setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();  // Get email input
        String password = passwordEditText.getText().toString().trim();  // Get password input

        // Check if email or password is empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(ManagersLogin.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to sign in the user with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();  // Get the current logged-in user
                        if (user != null) {
                            // Retrieve user data from Firestore
                            db.collection("managers")  // Collection of employees
                                    .document(user.getUid())  // Get the document corresponding to the user by UID
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();  // Get the document data
                                            if (document.exists()) {
                                                // If the document exists, the user is in the employees collection
                                                Log.d("Firebase", "User found in Firestore!");
                                                Intent intent = new Intent(ManagersLogin.this, EmployeeHomePage.class);
                                                startActivity(intent);
                                                finish();  // Close the current login activity
                                            } else {
                                                // If no document is found for the user in the employees collection
                                                Toast.makeText(ManagersLogin.this, "User not found in employees collection", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(ManagersLogin.this, "Failed to retrieve user data from Firestore", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Log.d("Firebase", "Authentication failed: " + task.getException());  // Log the error
                        Toast.makeText(ManagersLogin.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Method to navigate to the Registration activity when the registration button is clicked
    public void register(View v) {
        Intent intent = new Intent(ManagersLogin.this, Registration.class);  // Create an Intent to navigate to the Registration activity
        startActivity(intent);  // Start the Registration activity
    }

    public void forgotPassword(View v) {
        Intent intent = new Intent(ManagersLogin.this, ForgotPassword.class);  // Create an Intent to navigate to the Registration activity
        startActivity(intent);  // Start the Registration activity
    }
    public void Login(View v) {
        Intent intent = new Intent(ManagersLogin.this, ManagerHomePage.class);  // Create an Intent to navigate to the Registration activity
        startActivity(intent);  // Start the ManagerHomePage activity
    }
}
