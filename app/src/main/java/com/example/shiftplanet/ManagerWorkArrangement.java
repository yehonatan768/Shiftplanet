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

public class ManagerWorkArrangement extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String managerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manager_work_arrangement_page);

        // Retrieve the email
        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            managerEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.manager_work_arrangement);
        navigationView = findViewById(R.id.nav_view1);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        String message = "";

        if (item.getItemId() == R.id.m_my_profile) {
            message = "My profile clicked";
            intent = new Intent(ManagerWorkArrangement.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            message = "Employees requests clicked";
            intent = new Intent(ManagerWorkArrangement.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            message = "Build work arrangement clicked";
            // Remove unnecessary navigation to the same activity
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            message = "Published work arrangement clicked";
            // Remove unnecessary navigation to the same activity
        } else if (item.getItemId() == R.id.send_notifications) {
            message = "Send notifications clicked";
            intent = new Intent(ManagerWorkArrangement.this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            message = "Sent notifications clicked";
            intent = new Intent(ManagerWorkArrangement.this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            message = "Log out clicked";
            intent = new Intent(ManagerWorkArrangement.this, Login.class);
            startActivity(intent);
            finish();  // Only finish the activity when logging out
            return true;
        }

        if (intent != null) {
            showToastThenNavigate(message, intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Displays a toast and navigates to a new activity after a short delay
     */
    private void showToastThenNavigate(String message, Intent intent) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        intent.putExtra("LOGIN_EMAIL", managerEmail);
        new android.os.Handler().postDelayed(() -> {
            startActivity(intent);
            // Don't finish the current activity immediately
        }, 500);
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