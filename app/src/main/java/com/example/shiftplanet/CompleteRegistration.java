package com.example.shiftplanet;

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

public class CompleteRegistration extends AppCompatActivity {

    private static final String TAG = "CompleteRegistration";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText businessCodeEditText, idEditText, managerEmailEditText;
    private AutoCompleteTextView autoCompleteUserType;
    private String[] userTypes = {"Manager", "Employee"};
    private String fullName, email, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Retrieve Google Data
        Intent intent = getIntent();
        fullName = intent.getStringExtra("FULL_NAME");
        email = intent.getStringExtra("EMAIL");
        phone = intent.getStringExtra("PHONE");

        initializeUI();
        setButtonListeners();
    }

    private void initializeUI() {
        businessCodeEditText = findViewById(R.id.inputBusinessCode);
        idEditText = findViewById(R.id.input_id);
        managerEmailEditText = findViewById(R.id.input_manager_email);
        autoCompleteUserType = findViewById(R.id.autoCompleteUserType);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.user_type_list, userTypes);
        autoCompleteUserType.setAdapter(adapter);

        autoCompleteUserType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = parent.getItemAtPosition(position).toString();
            if ("Employee".equals(selectedType)) {
                managerEmailEditText.setVisibility(View.VISIBLE);
            } else {
                managerEmailEditText.setVisibility(View.GONE);
            }
        });
    }

    private void setButtonListeners() {
        findViewById(R.id.btn_complete_register).setOnClickListener(v -> saveUserToFirestore());
    }

    private void saveUserToFirestore() {
        String userType = autoCompleteUserType.getText().toString().trim();
        String businessCode = businessCodeEditText.getText().toString().trim();
        String id = idEditText.getText().toString().trim();
        String managerEmail = managerEmailEditText.getText().toString().trim();

        if (userType.isEmpty() || businessCode.isEmpty() || id.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User authentication failed", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullname", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("userType", userType);
        userData.put("businessCode", businessCode);
        userData.put("id", id);

        if ("Employee".equals(userType)) {
            userData.put("managerEmail", managerEmail);
        }

        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully.");
                    navigateToHomePage(email, fullName, phone);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    Toast.makeText(this, "Error saving data. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToHomePage(String email, String fullName, String phone) {
        Intent intent;
        if ("Manager".equalsIgnoreCase(autoCompleteUserType.getText().toString())) {
            intent = new Intent(CompleteRegistration.this, ManagerHomePage.class);
        } else {
            intent = new Intent(CompleteRegistration.this, EmployeeHomePage.class);
        }

        intent.putExtra("LOGIN_EMAIL", email);
        intent.putExtra("USER_NAME", fullName);
        intent.putExtra("USER_PHONE", phone);

        startActivity(intent);
        finish();
    }
}
