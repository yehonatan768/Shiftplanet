package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseUser;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private  FirebaseFirestore usersdb;
    private EditText emailEditText, passwordEditText, usernameEditText, phoneEditText, fullnameEditText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        EdgeToEdge.enable(this);

        Button back = findViewById(R.id.back_btn);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, EmployeesLogin.class);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();
        usersdb = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username_input);
        passwordEditText = findViewById(R.id.password_input1);
        fullnameEditText = findViewById(R.id.fullname_input);
        phoneEditText = findViewById(R.id.phone_input);
        emailEditText = findViewById(R.id.email_input1);


        Button buttonRegister = findViewById(R.id.register_btn);
        buttonRegister.setOnClickListener(v -> registerUser());

        }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String fullname = fullnameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || phone.isEmpty() || username.isEmpty() || fullname.isEmpty() ) {
            Toast.makeText(Registration.this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    saveUserInfo(username, password, fullname, phone, email, currentUser);}
                Toast.makeText(Registration.this, "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Registration.this, EmployeeHomePage.class);
                startActivity(intent);
            } else {
                Toast.makeText(Registration.this, "הרישום נכשל: " , Toast.LENGTH_SHORT).show();
                Log.e("FirebaseAuth", "Error: " + task.getException().getMessage());
            }
        });
    }

    private void saveUserInfo(String username, String password, String fullname, String phone, String email, FirebaseUser currentUser) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("fullname", fullname);
        user.put("phone", phone);
        user.put("email", email);

        usersdb.collection("users").document(currentUser.getUid())
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d("Firestore", "User data saved successfully!");
            })
            .addOnFailureListener(e -> {
                Log.e("Firestore", "Error saving user data");
            });
    }

}
