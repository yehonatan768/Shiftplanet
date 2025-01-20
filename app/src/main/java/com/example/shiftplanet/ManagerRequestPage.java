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
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerRequestPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String managerEmail;
    // Class variables to store query results
    private List<Map<String, String>> pendingRequests = new ArrayList<>();
    private List<Map<String, String>> closedRequests = new ArrayList<>();

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
        }
        // Fetch and store requests
        fetchRequests(() -> {
            // Once data is loaded, set the content view and initialize UI
            setContentView(R.layout.manager_request_page);
            initializeUI();
        });
    }

    private void initializeUI() {
        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
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
                .whereEqualTo("managerEmail", managerEmail) // Filter by managerEmail
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingRequests.clear(); // Clear any existing data
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("employeeName", document.getString("employeeEmail")); // Replace with actual name field if available
                        request.put("requestType", document.getString("reason")); // Replace "reason" with the actual field

                        // Retrieve requestNumber and store as a string in the map
                        Long requestNumberLong = document.getLong("requestNumber");
                        if (requestNumberLong != null) {
                            request.put("requestNumber", String.valueOf(requestNumberLong.intValue()));
                        } else {
                            request.put("requestNumber", "0"); // Default or error value
                        }

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

        db.collection("Requests")
                .whereIn("status", Arrays.asList("approved", "denied"))
                .whereEqualTo("managerEmail", managerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    closedRequests.clear(); // Clear any existing data
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("employeeName", document.getString("employeeEmail")); // Replace with actual name field if available
                        request.put("requestType", document.getString("reason"));
                        request.put("status", document.getString("status")); // Include status
                        Long requestNumberLong = document.getLong("requestNumber");
                        if (requestNumberLong != null) {
                            request.put("requestNumber", String.valueOf(requestNumberLong.intValue()));
                        } else {
                            request.put("requestNumber", "0"); // Default or error value
                        }
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



    private void populateRequests(List<Map<String, String>> requests, LinearLayout parentLayout, int defaultBackgroundColor) {
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
            String requestNumberStr = request.get("requestNumber");
            String status = request.get("status"); // Get the status field

            int requestNumber = 0;
            if (requestNumberStr != null && requestNumberStr.matches("\\d+")) {
                requestNumber = Integer.parseInt(requestNumberStr);
            }

            // Set background color based on status
            int backgroundColor = defaultBackgroundColor; // Default color for pending requests
            if ("approved".equalsIgnoreCase(status)) {
                backgroundColor = getResources().getColor(android.R.color.holo_green_dark); // Green for approved
            } else if ("denied".equalsIgnoreCase(status)) {
                backgroundColor = getResources().getColor(android.R.color.holo_red_dark); // Red for denied
            }

            LinearLayout requestLayout = createRequestLayout(
                    idCounter,
                    name,
                    requestType,
                    backgroundColor,
                    requestNumber
            );
            parentLayout.addView(requestLayout);

            idCounter++;
        }
    }


    private LinearLayout createRequestLayout(int id, String employeeEmail, String requestType, int backgroundColor, int number) {
        LinearLayout requestLayout = new LinearLayout(this);
        requestLayout.setId(id); // Set the unique ID
        requestLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set LayoutParams with a bottom margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120 // Height in px (adjust as needed)
        );
        layoutParams.setMargins(0, 8, 0, 8); // Add bottom margin
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
        nameTextView.setText(employeeEmail);
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

        // Set OnClickListener to navigate to ManagerDialogRequestDetails

        requestLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerRequestPage.this, ManagerDialogRequestDetails.class);
            intent.putExtra("managerEmail", managerEmail);
            intent.putExtra("employeeEmail", employeeEmail);
            intent.putExtra("requestNumber", number); // Pass the unique request ID
            startActivity(intent);
        });

        return requestLayout;
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerRequestPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerRequestPage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerRequestPage.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            Toast.makeText(ManagerRequestPage.this, "Published work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerRequestPage.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerRequestPage.this, "Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        } else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerRequestPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            finish();
        }
        drawerLayout.closeDrawer(Gravity.LEFT);
        if (intent != null) startActivity(intent);
        return true;
    }
}
