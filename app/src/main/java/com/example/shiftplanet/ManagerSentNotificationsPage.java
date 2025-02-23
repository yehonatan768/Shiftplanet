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

public class ManagerSentNotificationsPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String managerEmail;
    private List<Map<String, String>> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the email
        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            managerEmail = email;
        } else {
            Log.e(TAG, "No email received");
            finish();  // Log out if email is not found
        }

        setContentView(R.layout.manager_sent_notification_page);
        initializeUI();
        fetchManagerNotifications(managerEmail);
    }

    private void initializeUI() {
        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.manager_sent_notification_drawer_layout);
        navigationView = findViewById(R.id.manager_sent_notification_nav_view);
        toolbar = findViewById(R.id.manager_sent_notifications_toolbar);

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

    private void fetchManagerNotifications(String managerEmail) {
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
                        notification.put("updateType", document.getString("updateType"));  // סוג העדכון
                        notification.put("message", document.getString("message"));  // תיאור קצר של העדכון
                        notification.put("title", document.getString("title"));  // הוסף את ה-title
                        notifications.add(notification);
                    }
                    updateUI(); // Update the UI after fetching data
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManagerSentNotificationsPage.this, "Failed to load notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUI(); // Still update the UI even if there's an error
                });
    }


    private void updateUI() {
        LinearLayout pendingLayout = findViewById(R.id.layout_notifications);
        if (notifications.isEmpty()) {
            // Show no notifications message
            TextView noRequestsMessage = new TextView(this);
            noRequestsMessage.setText("No notifications available.");
            noRequestsMessage.setTextColor(getResources().getColor(android.R.color.white));
            noRequestsMessage.setTextSize(16);
            noRequestsMessage.setGravity(Gravity.CENTER);
            pendingLayout.addView(noRequestsMessage);
        } else {
            populateRequests(notifications, pendingLayout, getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    private void populateRequests(List<Map<String, String>> requests, LinearLayout parentLayout, int backgroundColor) {
        // Clear any previous views
        parentLayout.removeAllViews();

        // Dynamically add each request to the layout
        int idCounter = 1; // Start the ID counter
        for (Map<String, String> request : requests) {
            String notificationId = request.get("notificationId");
            String updateType = request.get("updateType") != null ? request.get("updateType") : "Update";  // Default to "Update"
            String message = request.get("message");
            String title = request.get("title");  // הוצאת השם של הכותרת

            LinearLayout requestLayout = createRequestLayout(idCounter, updateType, message, title, backgroundColor);

            // Set click listener to open detailed notification
            requestLayout.setOnClickListener(v -> {
                if (notificationId != null) {
                    Intent intent = new Intent(ManagerSentNotificationsPage.this, NotificationDetailActivityPage.class);
                    intent.putExtra("notificationId", notificationId);  // Send the request ID to the next activity
                    intent.putExtra("LOGIN_EMAIL", managerEmail);
                    startActivity(intent);
                } else {
                    Toast.makeText(ManagerSentNotificationsPage.this, "Notification ID is missing", Toast.LENGTH_SHORT).show();
                }
            });

            parentLayout.addView(requestLayout);
            idCounter++;
        }
    }
    private LinearLayout createRequestLayout(int id, String updateType, String message, String title, int backgroundColor) {
        LinearLayout requestLayout = new LinearLayout(this);
        requestLayout.setId(id); // Set the unique ID
        requestLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set LayoutParams with a bottom margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120 // Height in px (adjust as needed)
        );
        layoutParams.setMargins(0, 8, 0, 8); // Add bottom margin of 8px
        requestLayout.setLayoutParams(layoutParams);

        requestLayout.setPadding(12, 12, 12, 12);
        requestLayout.setBackgroundColor(backgroundColor);

        // Title TextView
        TextView titleTextView = new TextView(this);
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Take the remaining space
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        titleTextView.setText(title);
        titleTextView.setTextColor(getResources().getColor(android.R.color.white));
        titleTextView.setTextSize(16);
        titleTextView.setGravity(Gravity.CENTER_VERTICAL);

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

        // Message TextView
        TextView messageTextView = new TextView(this);
        messageTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        messageTextView.setText(message);
        messageTextView.setTextColor(getResources().getColor(android.R.color.white));
        messageTextView.setTextSize(16);
        messageTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

        // Add TextViews to the request layout
        requestLayout.addView(titleTextView);  // Add the title view first
        requestLayout.addView(updateTypeTextView);
        requestLayout.addView(messageTextView);

        return requestLayout;
    }


    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerSentNotificationsPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSentNotificationsPage.this, Profile.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerSentNotificationsPage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSentNotificationsPage.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerSentNotificationsPage.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSentNotificationsPage.this, ManagerWorkArrangement.class);
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            Toast.makeText(ManagerSentNotificationsPage.this, "Published work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSentNotificationsPage.this, ManagerWorkArrangement.class);
        } else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerSentNotificationsPage.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSentNotificationsPage.this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerSentNotificationsPage.this, "Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSentNotificationsPage.this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerSentNotificationsPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSentNotificationsPage.this, Login.class);
        }

        if (intent != null) {
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}