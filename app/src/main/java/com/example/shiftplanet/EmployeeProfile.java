package com.example.shiftplanet;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_SHORT;

import static com.example.shiftplanet.Registration.validPasswordCheck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmployeeProfile extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, fullNameEditText, phoneEditText;
    private Button emailUpdateBtn, passwordUpdateBtn, fullNameUpdateBtn, phoneUpdateBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String employeeEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String email = getIntent().getStringExtra("LOGIN_EMAIL");
        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            employeeEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }
        setContentView(R.layout.employee_profile);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeUI();

        // אתחול הרכיבים מה-XML
        emailEditText = findViewById(R.id.updated_email);
        passwordEditText = findViewById(R.id.updated_password);
        fullNameEditText = findViewById(R.id.updated_full_name);
        phoneEditText = findViewById(R.id.updated_phone);

        emailUpdateBtn = findViewById(R.id.updated_email_btn);
        passwordUpdateBtn = findViewById(R.id.updated_password_btn);
        fullNameUpdateBtn = findViewById(R.id.updated_full_name_btn);
        phoneUpdateBtn = findViewById(R.id.updated_phone_btn);


        fullNameUpdateBtn.setOnClickListener(view -> {
            String newFullName = fullNameEditText.getText().toString().trim();
            if (newFullName.isEmpty()) {
                Toast.makeText(EmployeeProfile.this, "enter updated full name", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("fullname", newFullName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EmployeeProfile.this, "Full name successfully updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EmployeeProfile.this, "error updating full name", Toast.LENGTH_SHORT).show();
                    });
        });


        phoneUpdateBtn.setOnClickListener(view -> {
            String newPhone = phoneEditText.getText().toString().trim();
            if (newPhone.isEmpty()) {
                Toast.makeText(EmployeeProfile.this, "enter updated phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("phone", newPhone)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EmployeeProfile.this, "phone number successfully updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EmployeeProfile.this, "error updating phone number", Toast.LENGTH_SHORT).show();
                    });
        });

        passwordUpdateBtn.setOnClickListener(view -> {
            String newPassword = passwordEditText.getText().toString().trim();
            if (newPassword.isEmpty()) {
                Toast.makeText(EmployeeProfile.this, "enter updated password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validPasswordCheck(newPassword)) {
                Toast.makeText(this, "password must contain 6 chars, at least 1 uppercase letter and at least 1 number ", LENGTH_SHORT).show();
                return;
            }

            // עדכון הסיסמה ב-Firebase Authentication
            mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmployeeProfile.this, "password successfully updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EmployeeProfile.this, "error updating password", Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        emailUpdateBtn.setOnClickListener(view -> {
            String newEmail = emailEditText.getText().toString().trim();

            if (newEmail.isEmpty()) {
                Toast.makeText(EmployeeProfile.this, "enter updated email", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
// שליחה לאישור הכתובת החדשה לפני עדכון
            user.verifyBeforeUpdateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // הכתובת עודכנה אחרי שהמשתמש אישר את הדוא"ל
                            Log.d("Email Update", "Email verification sent.");
                            Toast.makeText(EmployeeProfile.this, "email sent to verification", Toast.LENGTH_SHORT).show();
                            String userId = user.getUid();
                            db.collection("users").document(userId)
                                    .update("email", newEmail)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Email successfully updated in Firestore");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating email in Firestore", e);
                                    });

                        } else {
                            // טיפול בשגיאה
                            Log.e("Email Update", "Error sending email verification", task.getException());
                            Toast.makeText(EmployeeProfile.this, "error sending email to verification", Toast.LENGTH_SHORT).show();

                        }
                    });

        });

    }


    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeProfile.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, EmployeeProfile.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeProfile.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeProfile.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeProfile.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeProfile.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, EmployeeShiftChangeRequest.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeProfile.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeProfile.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeProfile.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeProfile.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();

    }

    private void initializeUI() {
        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.profile_employee);
        navigationView = findViewById(R.id.nav_view2);
        toolbar = findViewById(R.id.toolbar);

        // Set Toolbar as the ActionBar
        setSupportActionBar(toolbar);

        // Setup Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup NavigationView listener
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            handleNavigationItemSelected(menuItem);
            drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        });
    }
}