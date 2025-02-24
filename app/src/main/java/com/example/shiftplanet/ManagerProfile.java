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

public class ManagerProfile extends AppCompatActivity {

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

        setContentView(R.layout.manager_profile);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeUI();

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
                Toast.makeText(ManagerProfile.this, "enter updated full name", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("fullname", newFullName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ManagerProfile.this, "Full name successfully updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManagerProfile.this, "error updating full name", Toast.LENGTH_SHORT).show();
                    });
        });


        phoneUpdateBtn.setOnClickListener(view -> {
            String newPhone = phoneEditText.getText().toString().trim();
            if (newPhone.isEmpty()) {
                Toast.makeText(ManagerProfile.this, "enter updated phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("phone", newPhone)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ManagerProfile.this, "phone number successfully updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManagerProfile.this, "error updating phone number", Toast.LENGTH_SHORT).show();
                    });
        });

        passwordUpdateBtn.setOnClickListener(view -> {
            String newPassword = passwordEditText.getText().toString().trim();
            if (newPassword.isEmpty()) {
                Toast.makeText(ManagerProfile.this, "enter updated password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validPasswordCheck(newPassword)) {
                Toast.makeText(this, "password must contain 6 chars, at least 1 uppercase letter and at least 1 number ", LENGTH_SHORT).show();
                return;
            }

            mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManagerProfile.this, "password successfully updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManagerProfile.this, "error updating password", Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        emailUpdateBtn.setOnClickListener(view -> {
            String newEmail = emailEditText.getText().toString().trim();

            if (newEmail.isEmpty()) {
                Toast.makeText(ManagerProfile.this, "enter updated email", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            user.verifyBeforeUpdateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Email Update", "Email verification sent.");
                            Toast.makeText(ManagerProfile.this, "email sent to verification", Toast.LENGTH_SHORT).show();
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
                            Log.e("Email Update", "Error sending email verification", task.getException());
                            Toast.makeText(ManagerProfile.this, "error sending email to verification", Toast.LENGTH_SHORT).show();

                        }
                    });

        });

    }


    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() ==  R.id.m_home_page) {
            Toast.makeText(ManagerProfile.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerProfile.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerProfile.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerProfile.this, ManagerProfile.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerProfile.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerProfile.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerProfile.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerProfile.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerProfile.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerProfile.this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerProfile.this, "Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerProfile.this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerProfile.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerProfile.this, Login.class);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
    }

        private void initializeUI() {
        drawerLayout = findViewById(R.id.profile_manager);
        navigationView = findViewById(R.id.nav_view3);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            handleNavigationItemSelected(menuItem);
            drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        });
    }
}