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
                                fetchManagerNotifications(managerEmail, onComplete);
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
                            notification.put("title", document.getString("title"));  // Add the title field
                            notification.put("updateType", document.getString("updateType"));  // סוג העדכון
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
                        Intent intent = new Intent(EmployeeNotificationsPage.this, NotificationDetailActivityPage.class);
                        intent.putExtra("notificationId", notificationId);  // Send the request ID to the next activity
                        intent.putExtra("LOGIN_EMAIL", employeeEmail);
                        startActivity(intent);
                    } else {
                        Toast.makeText(EmployeeNotificationsPage.this, "Notification ID is missing", Toast.LENGTH_SHORT).show();
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

            // Set LayoutParams with a bottom margin to ensure consistent height
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
            if (item.getItemId() == R.id.e_my_profile) {
                Toast.makeText(EmployeeNotificationsPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
                intent = new Intent(EmployeeNotificationsPage.this, EmployeeHomePage.class);
                intent.putExtra("LOGIN_EMAIL", employeeEmail);
            } else if (item.getItemId() == R.id.e_work_arrangement) {
                Toast.makeText(EmployeeNotificationsPage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
                intent = new Intent(EmployeeNotificationsPage.this, EmployeeHomePage.class);
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
                intent = new Intent(EmployeeNotificationsPage.this, EmployeeHomePage.class);
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