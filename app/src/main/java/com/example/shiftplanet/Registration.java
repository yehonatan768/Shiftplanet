package com.example.shiftplanet;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    String[] userTypes = {"Manager", "Employee"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    private FirebaseAuth mAuth;
    private FirebaseFirestore usersdb;

    EditText confirmPassword, passwordEditText, emailEditText, phoneEditText, fullnameEditText, businessCodeEditText, idEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        // Back button
        ImageView back = findViewById(R.id.back_btn);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersdb = FirebaseFirestore.getInstance();

        // Find views
        emailEditText = findViewById(R.id.inputEmail);
        passwordEditText = findViewById(R.id.inputPassword);
        fullnameEditText = findViewById(R.id.input_full_name);
        phoneEditText = findViewById(R.id.inputPhoneNumber);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
        autoCompleteTextView = findViewById(R.id.autoCompleteUserType);
        businessCodeEditText = findViewById(R.id.inputBusinessCode);
        idEditText = findViewById(R.id.input_id);

        // Setup dropdown
        adapterItems = new ArrayAdapter<>(this, R.layout.user_type_list, userTypes);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(Registration.this, "Selected: " + item, LENGTH_SHORT).show();
        });

        // Register button
        Button buttonRegister = findViewById(R.id.btnRegister);
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();
        String fullname = fullnameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String userType = autoCompleteTextView.getText().toString().trim();
        String businessCode = businessCodeEditText.getText().toString().trim();
        String id = idEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPasswordText.isEmpty() || fullname.isEmpty() || phone.isEmpty() || userType.isEmpty() || businessCode.isEmpty() || id.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPasswordText)) {
            Toast.makeText(this, "Passwords do not match", LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Send email verification
                    currentUser.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            saveUserInfo(fullname, phone, email, userType, businessCode, id, currentUser);
                            Toast.makeText(this, "Registration Completed. Please verify your email!", LENGTH_SHORT).show();
                            Intent intent = new Intent(Registration.this, Login.class); // Redirect to login
                            startActivity(intent);
                        } else {
                            String errorMessage = verificationTask.getException() != null
                                    ? verificationTask.getException().getMessage()
                                    : "Unknown error while sending verification email";
                            Toast.makeText(this, "Failed to send verification email: " + errorMessage, LENGTH_SHORT).show();
                            Log.e("EmailVerification", "Error: " + errorMessage);
                        }
                    });
                }
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(this, "Registration failed: " + errorMessage, LENGTH_SHORT).show();
                Log.e("FirebaseAuth", "Error: " + errorMessage);
            }
        });
    }


    private void saveUserInfo(String fullname, String phone, String email, String userType, String businessCode, String id, FirebaseUser currentUser) {
        Map<String, Object> user = new HashMap<>();
        user.put("fullname", fullname);
        user.put("phone", phone);
        user.put("email", email);
        user.put("userType", userType);
        user.put("businessCode", businessCode);
        user.put("id", id);

        usersdb.collection("users").document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data saved successfully!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user data", e));
    }
}
