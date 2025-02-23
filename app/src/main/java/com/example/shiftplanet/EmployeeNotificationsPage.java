package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class EmployeeNotificationsPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String employeeEmail;
    private List<Map<String, String>> notifications = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the email
        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            employeeEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }
        // Fetch and store requests
        fetchNotifications(() -> {
            // Once data is loaded, set the content view and initialize UI
            setContentView(R.layout.employee_notifications_page);
            initializeUI();
        });

    }
    private void initializeUI() {
        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.employee_notifications_drawer_layout);
        navigationView = findViewById(R.id.employee_notifications_nav_view);
        toolbar = findViewById(R.id.employee_notifications_toolbar);

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
        // Populate requests into UI
        LinearLayout pendingLayout = findViewById(R.id.layout_notifications);


        populateRequests(notifications, pendingLayout, getResources().getColor(android.R.color.holo_blue_light));

    }
    private void fetchNotifications(Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch the employee's details to get their manager's email
        db.collection("users")  // Assuming "Employees" is the collection with employee data
                .whereEqualTo("email", employeeEmail)  // Find the employee by their email
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot employeeDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String managerEmail = employeeDoc.getString("managerEmail");  // Retrieve the manager's email

                        if (managerEmail != null) {
                            // Now that we have the manager's email, fetch the notifications for that manager

                            AtomicInteger pendingRequests = new AtomicInteger(2);

                            Runnable completionCallback = () -> {
                                if (pendingRequests.decrementAndGet() == 0) {
                                    onComplete.run(); // מפעילים את onComplete רק אחרי ששתי השאילתות הסתיימו
                                }
                            };

                            fetchManagerNotifications(managerEmail, completionCallback);
                            fetchShiftChangeRequests(managerEmail, completionCallback);

                        } else {
                            Toast.makeText(EmployeeNotificationsPage.this, "Manager email not found.", Toast.LENGTH_SHORT).show();
                            onComplete.run();  // Proceed even if manager email is missing
                        }
                    } else {
                        Toast.makeText(EmployeeNotificationsPage.this, "Employee not found.", Toast.LENGTH_SHORT).show();
                        onComplete.run();  // Proceed if employee data not found
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeNotificationsPage.this, "Failed to load employee details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onComplete.run();  // Proceed if there's an error fetching employee data
                });
    }

    private void fetchManagerNotifications(String managerEmail, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch notifications based on manager's email
        db.collection("Notifications")
                .whereEqualTo("managerEmail", managerEmail)  // Ensure the notification is from the manager
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifications.clear();  // Clear any existing data
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> notification = new HashMap<>();
                        notification.put("notificationId", document.getId());
                        notification.put("updateType", "Manager Update");  // סוג העדכון
                        notification.put("message", document.getString("message"));  // תיאור קצר של העדכון
                        notifications.add(notification);
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeNotificationsPage.this, "Failed to load notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onComplete.run();
                });
    }


    private void fetchShiftChangeRequests(String managerEmail, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("ShiftChangeRequests")
                .whereEqualTo("managerEmail", managerEmail) // מסנן רק בקשות שמיועדות למנהל הזה
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> shiftRequest = new HashMap<>();
                        shiftRequest.put("notificationId", document.getId());
                        shiftRequest.put("updateType", "Shift Change Request");
                        shiftRequest.put("message", document.getString("employeeEmail") + " requested a shift change.");
                        notifications.add(shiftRequest);
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeNotificationsPage.this, "Failed to load shift change requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onComplete.run();
                });
    }


    private void populateRequests(List<Map<String, String>> requests, LinearLayout parentLayout, int backgroundColor) {
        // Clear any previous views
        parentLayout.removeAllViews();

        if (requests.isEmpty()) {
            // Add a "No Requests" message if the list is empty
            TextView noRequestsMessage = new TextView(this);
            noRequestsMessage.setText("No notifications available.");
            noRequestsMessage.setTextColor(getResources().getColor(android.R.color.white));
            noRequestsMessage.setTextSize(16);
            noRequestsMessage.setGravity(Gravity.CENTER);
            parentLayout.addView(noRequestsMessage);
            return;
        }

        // Dynamically add each request to the layout
        int idCounter = 1; // Start the ID counter
        for (Map<String, String> request : requests) {
            String notificationId = request.get("notificationId");
            String updateType = request.get("updateType");
            String message = request.get("message");

            LinearLayout requestLayout = createRequestLayout(
                    idCounter,
                    updateType,  // Type of update (e.g., "Vacation Approved", "Sick Leave")
                    "",     // A brief message or description of the update
                    backgroundColor
            );

            // Set click listener to open detailed notification
            requestLayout.setOnClickListener(v -> {
                if (notificationId != null) {
                    Intent intent;

                    if ("Manager Update".equals(updateType)) {
                        intent = new Intent(EmployeeNotificationsPage.this, NotificationDetailActivityPage.class);
                        intent.putExtra("notificationId", notificationId);  // Send the request ID to the next activity
                        intent.putExtra("LOGIN_EMAIL", employeeEmail);
                    }
                    // אם סוג העדכון הוא "החלפה"
                    else if ("Shift Change Request".equals(updateType)) {
                        intent = new Intent(EmployeeNotificationsPage.this, EmployeeShiftChangeDialog.class); // כאן את שולחת לעמוד דיאלוג החלפה
                        intent.putExtra("notificationId", notificationId);  // תעביר גם את ה- notificationId לעמוד של החלפה, אם צריך
                        intent.putExtra("LOGIN_EMAIL", employeeEmail);
                    }
                    else {
                        Toast.makeText(EmployeeNotificationsPage.this, "Unknown update type", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(intent);
                } else {
                    Toast.makeText(EmployeeNotificationsPage.this, "Notification ID is missing", Toast.LENGTH_SHORT).show();
                }
            });

            parentLayout.addView(requestLayout);

            idCounter++;
        }
    }


    private LinearLayout createRequestLayout(int id, String updateType, String message, int backgroundColor) {
        LinearLayout requestLayout = new LinearLayout(this);
        requestLayout.setId(id); // Set the unique ID
        requestLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set LayoutParams with a bottom margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120 // Height in px (adjust as needed)
        );
        layoutParams.setMargins(0, 8, 0, 8); // Add bottom margin of 16px
        requestLayout.setLayoutParams(layoutParams);

        requestLayout.setPadding(12, 12, 12, 12);
        requestLayout.setBackgroundColor(backgroundColor);

        // Update Type TextView
        TextView updateTypeTextView = new TextView(this);
        updateTypeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0, // Width = fill remaining space
                LinearLayout.LayoutParams.MATCH_PARENT,
                1 // Weight
        ));
        updateTypeTextView.setText(updateType);
        updateTypeTextView.setTextColor(getResources().getColor(android.R.color.white));
        updateTypeTextView.setTextSize(16);
        updateTypeTextView.setGravity(Gravity.CENTER_VERTICAL);


        requestLayout.addView(updateTypeTextView);

        return requestLayout;
    }

    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeNotificationsPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, Profile.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeNotificationsPage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, EmployeeWorkArrangement.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeNotificationsPage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeNotificationsPage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeNotificationsPage.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, EmployeeShiftChangeRequest.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeNotificationsPage.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeNotificationsPage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeNotificationsPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeNotificationsPage.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();

    }
}

