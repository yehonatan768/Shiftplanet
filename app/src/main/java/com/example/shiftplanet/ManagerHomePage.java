package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerHomePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String managerEmail;

        private TextView tvEmployeeName;
        private LinearLayout updatesLayout;

        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        private List<Map<String, String>> notifications = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);

            String email = getIntent().getStringExtra("LOGIN_EMAIL");
            if (email != null) {
                managerEmail = email;
            }

            setContentView(R.layout.manager_home_page);
            initializeUI();

        }

        private void initializeUI() {

            tvEmployeeName = findViewById(R.id.tv_manager_name);



            Button buildWorkArrangementButton = findViewById(R.id.build_work_arrangement_button);
            Button sendNotifications = findViewById(R.id.send_notifications_button);
            Button sentNotifications = findViewById(R.id.sent_notifications_button);
            Button employeesRequests = findViewById(R.id.employees_requests_button);




            buildWorkArrangementButton.setOnClickListener(v -> navigateToPage("Build Work Arrangement"));
            sendNotifications.setOnClickListener(v -> navigateToPage("Send Notifications"));
            sentNotifications.setOnClickListener(v -> navigateToPage("Sent Notifications"));
            employeesRequests.setOnClickListener(v -> navigateToPage("Employees Requests"));


            getEmployeeName();
            drawerLayout = findViewById(R.id.manager_home_page);
            navigationView = findViewById(R.id.manager_home_page_nav_view);
            toolbar = findViewById(R.id.manager_home_page_toolbar);
            setSupportActionBar(toolbar);



            setSupportActionBar(toolbar);

            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(menuItem -> {
                onNavigationItemSelected(menuItem);
                drawerLayout.closeDrawer(Gravity.LEFT);
                return true;
            });
        }

        private void getEmployeeName() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                db.collection("users").document(user.getUid()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String fullName = documentSnapshot.getString("fullname");
                                tvEmployeeName.setText("Hello " + fullName);
                            }
                        })
                        .addOnFailureListener(e -> Log.e("Error", "Error fetching user name", e));
            }
        }


        private void navigateToPage(String buttonName) {
            Intent intent = null;
            if (buttonName.equals("Build Work Arrangement")) {
                intent = new Intent(ManagerHomePage.this, ManagerWorkArrangement.class);
            }  else if (buttonName.equals("Send Notifications")) {
                intent = new Intent(ManagerHomePage.this, ManagerSendNotificationPage.class);
            } else if (buttonName.equals("Sent Notifications")) {
                intent = new Intent(ManagerHomePage.this, ManagerSentNotificationsPage.class);
            } else if (buttonName.equals("Employees Requests")) {
                intent = new Intent(ManagerHomePage.this, ManagerRequestPage.class);
            }

            if (intent != null) {
                intent.putExtra("LOGIN_EMAIL", managerEmail);
                startActivity(intent);
            }
        }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        String message = "";
        if (item.getItemId() ==  R.id.m_home_page) {
            message = "Home Page clicked";
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
        } else if (item.getItemId() ==  R.id.m_my_profile) {
            message = "My profile clicked";
            intent = new Intent(ManagerHomePage.this, ManagerProfile.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            message = "Employees requests clicked";
            intent = new Intent(ManagerHomePage.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            message = "Build work arrangement clicked";
            intent = new Intent(ManagerHomePage.this, ManagerWorkArrangement.class);
        } else if (item.getItemId() == R.id.send_notifications) {
            message = "Send notifications clicked";
            intent = new Intent(ManagerHomePage.this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            message = "Sent notifications clicked";
            intent = new Intent(ManagerHomePage.this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            message = "Logging out...";
            intent = new Intent(ManagerHomePage.this, Login.class);
        }

        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        if (intent != null) {
            showToastThenNavigate(message, intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showToastThenNavigate(String message, Intent intent) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        intent.putExtra("LOGIN_EMAIL", managerEmail);

        new android.os.Handler().postDelayed(() -> {
            startActivity(intent);
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