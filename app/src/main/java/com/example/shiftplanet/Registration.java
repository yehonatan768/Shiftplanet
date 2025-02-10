package com.example.shiftplanet;

import static android.widget.Toast.LENGTH_SHORT;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
    private FirebaseFirestore db;

    EditText managerEmailEditText, confirmPassword, passwordEditText, emailEditText, phoneEditText, fullnameEditText, businessCodeEditText, idEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        initializeUI();
        initializeFirebase();
        setButtonListeners();

    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeUI() {
        emailEditText = findViewById(R.id.inputEmail);
        passwordEditText = findViewById(R.id.inputPassword);
        confirmPassword = findViewById(R.id.inputConfirmPassword);
        fullnameEditText = findViewById(R.id.input_full_name);
        phoneEditText = findViewById(R.id.inputPhoneNumber);
        autoCompleteTextView = findViewById(R.id.autoCompleteUserType);
        businessCodeEditText = findViewById(R.id.inputBusinessCode);
        idEditText = findViewById(R.id.input_id);
        managerEmailEditText=findViewById(R.id.input_manager_email);

        // Setup dropdown
        adapterItems = new ArrayAdapter<>(this, R.layout.user_type_list, userTypes);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(Registration.this, "Selected: " + item, LENGTH_SHORT).show();
        });

        //Setup employee visibility of manager email
        managerEmailEditText = findViewById(R.id.input_manager_email);
        autoCompleteTextView = findViewById(R.id.autoCompleteUserType);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUserType = parent.getItemAtPosition(position).toString();
            if (selectedUserType.equals("Employee")) {
                managerEmailEditText.setVisibility(View.VISIBLE);
            } else {
                managerEmailEditText.setVisibility(View.GONE);
            }
        });
    }
    private void setButtonListeners() {
        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
        findViewById(R.id.back_btn).setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
        });
        findViewById(R.id.alreadyHaveAccount).setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
        });
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
        String managerEmail;

        if ("Employee".equalsIgnoreCase(userType)) {
            managerEmail = managerEmailEditText.getText().toString().trim();
        } else {
            managerEmail = "";
        }

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
                            saveUserInfo(fullname, phone, email, userType, businessCode, id, managerEmail, currentUser);
                            Toast.makeText(this, "Registration Completed. Please verify your email!", LENGTH_SHORT).show();
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
        Intent intent = new Intent(Registration.this, Login.class); // Redirect to login
        startActivity(intent);
    }


    private void saveUserInfo(String fullname, String phone, String email, String userType, String businessCode, String id, String managerEmail, FirebaseUser currentUser) {
        Map<String, Object> user = new HashMap<>();
        user.put("fullname", fullname);
        user.put("phone", phone);
        user.put("email", email);
        user.put("id", id);
        user.put("businessCode", businessCode);
        user.put("userType", userType);

        if ("Employee".equalsIgnoreCase(userType)) {
            user.put("managerEmail", managerEmail);
        }

        db.collection("users").document(currentUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data saved successfully!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user data", e));
    }
}
