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

public class EmployeesLogin extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employees_login);

        emailEditText = findViewById(R.id.email_input1);
        passwordEditText = findViewById(R.id.password_input1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.login_btn1).setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // אם לא הוזן אימייל או סיסמה
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(EmployeesLogin.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // התחברות למערכת Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // חיפוש במאגר המידע של העובדים
                            db.collection("employees")
                                    .document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (document.exists()) {
                                                // משתמש נמצא בעובדים, עבר למסך הבית של העובד
                                                Log.d("Firebase", "User found in employees collection!");
                                                Intent intent = new Intent(EmployeesLogin.this, EmployeeHomePage.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // חיפוש במאגר המידע של המנהלים
                                                db.collection("managers")
                                                        .document(user.getUid())
                                                        .get()
                                                        .addOnCompleteListener(task2 -> {
                                                            if (task2.isSuccessful()) {
                                                                DocumentSnapshot managerDoc = task2.getResult();
                                                                if (managerDoc.exists()) {
                                                                    // משתמש נמצא במנהלים, עבר למסך הבית של המנהל
                                                                    Log.d("Firebase", "User found in managers collection!");
                                                                    Intent intent = new Intent(EmployeesLogin.this, ManagerHomePage.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    // אם לא נמצא לא בעובדים ולא במנהלים
                                                                    Toast.makeText(EmployeesLogin.this, "User not found in any collection", Toast.LENGTH_SHORT).show();
                                                                }
                                                            } else {
                                                                // במקרה של שגיאה במהלך שליפת הנתונים
                                                                Log.e("Firebase", "Failed to retrieve manager data", task2.getException());
                                                                Toast.makeText(EmployeesLogin.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        } else {
                                            // במקרה של שגיאה בשאילתא
                                            Log.e("Firebase", "Failed to retrieve user data from Firestore", task1.getException());
                                            Toast.makeText(EmployeesLogin.this, "Failed to retrieve user data from Firestore", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        // במקרה של שגיאה בהתחברות
                        Log.e("Firebase", "Authentication failed: " + task.getException());
                        Toast.makeText(EmployeesLogin.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void register(View v) {
        Intent intent = new Intent(EmployeesLogin.this, Registration.class);
        startActivity(intent);
    }

    public void forgotPassword(View v) {
        Intent intent = new Intent(EmployeesLogin.this, ForgotPassword.class);
        startActivity(intent);
    }
}
