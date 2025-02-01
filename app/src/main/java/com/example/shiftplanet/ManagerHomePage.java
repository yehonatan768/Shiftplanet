package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class ManagerHomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String managerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // אפשרות לתמיכה בתצוגה בקצוות
        setContentView(R.layout.manager_home_page);

        // Retrieve the email
        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            managerEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }

        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.manager_home_page);
        navigationView = findViewById(R.id.nav_view1);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    private void saveFCMTokenToFirestore(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // יצירת HashMap עם ה-token
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmToken", token);

            // עדכון המסמך של המשתמש ב-Firestore עם ה-token החדש
            db.collection("users")
                    .document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // הודעה במקרה של הצלחה
                        // Toast.makeText(ManagerHomePage.this, "FCM Token updated successfully.", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> {
                        // הודעה במקרה של כישלון
                        Toast.makeText(ManagerHomePage.this, "Error updating FCM Token", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this, token -> {
                    // ה-token שהתקבל מ-FCM
                    saveFCMTokenToFirestore(token);
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(ManagerHomePage.this, "Error getting FCM Token", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerHomePage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerHomePage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerHomePage.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            Toast.makeText(ManagerHomePage.this, "Published work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerHomePage.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerSendNotificationPage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerHomePage.this, "\"Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerSentNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerHomePage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, Login.class);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}