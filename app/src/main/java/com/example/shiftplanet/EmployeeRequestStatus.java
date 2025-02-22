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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeRequestStatus extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String employeeEmail;
    private List<Map<String, String>> pendingRequests = new ArrayList<>();
    private List<Map<String, String>> closedRequests = new ArrayList<>();

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
        fetchRequests(() -> {
            // Once data is loaded, set the content view and initialize UI
            setContentView(R.layout.employee_request_status);
            initializeUI();
        });
    }
    private void initializeUI() {
        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout1);
        navigationView = findViewById(R.id.nav_view1);
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
        // Populate requests into UI
        LinearLayout pendingLayout = findViewById(R.id.layout_pending_requests);
        LinearLayout closedLayout = findViewById(R.id.layout_closed_requests);

        populateRequests(pendingRequests, pendingLayout, getResources().getColor(android.R.color.holo_blue_light));
        populateRequests(closedRequests, closedLayout, getResources().getColor(android.R.color.holo_red_light));
    }
    private void fetchRequests(Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch pending requests with the given managerEmail
        db.collection("Requests")
                .whereEqualTo("status", "pending")
                .whereEqualTo("employeeEmail", employeeEmail) // Filter by employeeEmail
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingRequests.clear(); // Clear any existing data
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("requestType", document.getString("reason")); // Replace "reason" with the actual field
                        request.put("requestStatus", document.getString("status")); // Replace with actual status
                        pendingRequests.add(request);
                    }

                    // Fetch closed requests after pending requests
                    fetchClosedRequests(onComplete);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load pending requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    fetchClosedRequests(onComplete); // Proceed to fetch closed requests
                });
    }
    private void fetchClosedRequests(Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query for requests where the status is either "approved" or "denied"
        db.collection("Requests")
                .whereIn("status", Arrays.asList("approved", "denied"))
                .whereEqualTo("employeeEmail", employeeEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    closedRequests.clear(); // Clear any existing data
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("requestType", document.getString("reason"));
                        request.put("status", document.getString("status")); // Replace with actual status
                        closedRequests.add(request);
                    }

                    // Call the callback once both queries are completed
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load approved/denied requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onComplete.run(); // Call the callback even if this query fails
                });
    }


    private void populateRequests(List<Map<String, String>> requests, LinearLayout parentLayout, int backgroundColor) {
        // Clear any previous views
        parentLayout.removeAllViews();

        if (requests.isEmpty()) {
            // Add a "No Requests" message if the list is empty
            TextView noRequestsMessage = new TextView(this);
            noRequestsMessage.setText("No requests available.");
            noRequestsMessage.setTextColor(getResources().getColor(android.R.color.white));
            noRequestsMessage.setTextSize(16);
            noRequestsMessage.setGravity(Gravity.CENTER);
            parentLayout.addView(noRequestsMessage);
            return;
        }

        // Dynamically add each request to the layout
        int idCounter = 1; // Start the ID counter
        for (Map<String, String> request : requests) {
            String name = request.get("employeeName");
            String requestType = request.get("requestType");

            LinearLayout requestLayout = createRequestLayout(
                    idCounter,
                    name,
                    requestType,
                    backgroundColor
            );
            parentLayout.addView(requestLayout);

            idCounter++;
        }
    }

    private LinearLayout createRequestLayout(int id, String name, String requestType, int backgroundColor) {
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

        // Name TextView
        TextView nameTextView = new TextView(this);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0, // Width = fill remaining space
                LinearLayout.LayoutParams.MATCH_PARENT,
                1 // Weight
        ));
        nameTextView.setText(name);
        nameTextView.setTextColor(getResources().getColor(android.R.color.white));
        nameTextView.setTextSize(16);
        nameTextView.setGravity(Gravity.CENTER_VERTICAL);

        // Request Type TextView
        TextView requestTypeTextView = new TextView(this);
        requestTypeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        requestTypeTextView.setText(requestType);
        requestTypeTextView.setTextColor(getResources().getColor(android.R.color.white));
        requestTypeTextView.setTextSize(16);
        requestTypeTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

        // Add TextViews to the request layout
        requestLayout.addView(nameTextView);
        requestLayout.addView(requestTypeTextView);

        return requestLayout;
    }


    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeRequestStatus.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeRequestStatus.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeRequestStatus.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeRequestStatus.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeRequestStatus.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeShiftChange.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeRequestStatus.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeRequestStatus.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeRequestStatus.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();

    }
}