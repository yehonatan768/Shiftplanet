
package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EmployeeHomePage extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private TextView tvEmployeeName;
    private LinearLayout updatesLayout;
    private String employeeEmail;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Map<String, String>> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        String email = getIntent().getStringExtra("LOGIN_EMAIL");
        if (email != null) {
            employeeEmail = email;
        }

        setContentView(R.layout.employee_home_page);
        initializeUI();
        fetchNotifications();
    }

    private void initializeUI() {

        tvEmployeeName = findViewById(R.id.tv_employee_name);
        updatesLayout = findViewById(R.id.updates_layout);


        Button workArrangementButton = findViewById(R.id.Work_Arrangement_button);
        Button submitConstraintsButton = findViewById(R.id.Submit_Constraints_button);
        Button dayOffRequestButton = findViewById(R.id.Day_off_request_button);
        Button shiftChangeRequestButton = findViewById(R.id.Shift_change_request_button);
        Button notificationsButton = findViewById(R.id.Notifications_button);
        Button requestStatusButton = findViewById(R.id.request_status_button);



        workArrangementButton.setOnClickListener(v -> navigateToPage("Work Arrangement"));
        submitConstraintsButton.setOnClickListener(v -> navigateToPage("Submit Constraints"));
        dayOffRequestButton.setOnClickListener(v -> navigateToPage("Day Off Request"));
        shiftChangeRequestButton.setOnClickListener(v -> navigateToPage("Shift Change Request"));
        notificationsButton.setOnClickListener(v -> navigateToPage("Notifications"));
        requestStatusButton.setOnClickListener(v -> navigateToPage("Requests Status"));

        getEmployeeName();
        drawerLayout = findViewById(R.id.employee_home_page);
        navigationView = findViewById(R.id.employee_home_page_nav_view);
        toolbar = findViewById(R.id.employee_home_page_toolbar);
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

    private void fetchNotifications() {
        db.collection("users")
                .whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot employeeDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String managerEmail = employeeDoc.getString("managerEmail");
                        if (managerEmail != null) {
                            fetchManagerNotifications(managerEmail);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EmployeeHomePage.this, "Failed to load employee details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchManagerNotifications(String managerEmail) {
        db.collection("Notifications")
                .whereEqualTo("managerEmail", managerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifications.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> notification = new HashMap<>();
                        notification.put("notificationId", document.getId());
                        notification.put("title", document.getString("title"));

                        notifications.add(notification);
                    }
                    populateRequests(notifications, updatesLayout, getResources().getColor(android.R.color.holo_blue_light));
                })
                .addOnFailureListener(e -> Toast.makeText(EmployeeHomePage.this, "Failed to load notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void populateRequests(List<Map<String, String>> notifications, LinearLayout updatesLayout, int backgroundColor) {
        updatesLayout.removeAllViews();
        if (notifications.isEmpty()) {
            TextView noRequestsMessage = new TextView(this);
            noRequestsMessage.setText("No notifications available.");
            noRequestsMessage.setTextColor(getResources().getColor(android.R.color.white));
            noRequestsMessage.setTextSize(16);
            noRequestsMessage.setGravity(Gravity.CENTER);
            updatesLayout.addView(noRequestsMessage);
            return;
        }

        for (Map<String, String> notification : notifications) {
            TextView notificationView = new TextView(this);
            String notificationId = notification.get("notificationId");
            notificationView.setText(notification.get("title") + "\n");
            notificationView.setTextColor(getResources().getColor(android.R.color.white));
            notificationView.setTextSize(16);
            notificationView.setPadding(16, 16, 16, 16);


            notificationView.setOnClickListener(v -> {
                if(notificationId!=null){
                    Intent intent = new Intent(EmployeeHomePage.this, NotificationDetailActivityPage.class);
                    intent.putExtra("notificationId", notificationId);
                    intent.putExtra("LOGIN_EMAIL", employeeEmail);
                    intent.putExtra("CLASS_NAME", "EmployeeHomePage");
                    startActivity(intent);
                }});

            updatesLayout.addView(notificationView);
        }
    }

    private void navigateToPage(String buttonName) {
        Intent intent = null;
        if (buttonName.equals("Work Arrangement")) {
            intent = new Intent(EmployeeHomePage.this, EmployeeHomePage.class);
        } else if (buttonName.equals("Submit Constraints")) {
            intent = new Intent(EmployeeHomePage.this, EmployeeSubmitConstraintsPage.class);
        } else if (buttonName.equals("Day Off Request")) {
            intent = new Intent(EmployeeHomePage.this, EmployeeRequestPage.class);
        } else if (buttonName.equals("Shift Change Request")) {
            intent = new Intent(EmployeeHomePage.this, EmployeeShiftChangeRequest.class);
        } else if (buttonName.equals("Notifications")) {
            intent = new Intent(EmployeeHomePage.this, EmployeeNotificationsPage.class);
        }else if (buttonName.equals("Requests Status")) {
            intent = new Intent(EmployeeHomePage.this, EmployeeRequestStatus.class);
        }

        if (intent != null) {
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
            startActivity(intent);
        }
    }
    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_home_page) {
            Toast.makeText(EmployeeHomePage.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeHomePage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeProfile.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeHomePage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeWorkArrangement.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeHomePage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeHomePage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeHomePage.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeShiftChangeRequest.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeHomePage.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeHomePage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeHomePage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeHomePage.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
    }

}
