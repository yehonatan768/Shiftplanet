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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerRequestPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String managerEmail;

    private List<Map<String, String>> pendingRequests = new ArrayList<>();
    private List<Map<String, String>> closedRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            managerEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }

        fetchRequests(() -> {

            setContentView(R.layout.manager_request_page);
            initializeUI();
        });
    }

    private void initializeUI() {

        drawerLayout = findViewById(R.id.manager_request_page);
        navigationView = findViewById(R.id.nav_view1);
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


        LinearLayout pendingLayout = findViewById(R.id.layout_pending_requests);
        LinearLayout closedLayout = findViewById(R.id.layout_closed_requests);

        populateRequests(pendingRequests, pendingLayout, getResources().getColor(android.R.color.holo_blue_light));
        populateRequests(closedRequests, closedLayout, getResources().getColor(android.R.color.holo_red_light));
    }

    private void fetchRequests(Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Requests")
                .whereEqualTo("status", "pending")
                .whereEqualTo("managerEmail", managerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingRequests.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("employeeName", document.getString("employeeEmail"));
                        request.put("requestType", document.getString("requestType"));

                        Long requestNumberLong = document.getLong("requestNumber");
                        if (requestNumberLong != null) {
                            request.put("requestNumber", String.valueOf(requestNumberLong.intValue()));
                        } else {
                            request.put("requestNumber", "0");
                        }

                        pendingRequests.add(request);
                    }

                    fetchClosedRequests(onComplete);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load pending requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    fetchClosedRequests(onComplete);
                });
    }


    private void fetchClosedRequests(Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Requests")
                .whereIn("status", Arrays.asList("approved", "denied"))
                .whereEqualTo("managerEmail", managerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    closedRequests.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("employeeName", document.getString("employeeEmail"));
                        request.put("requestType", document.getString("requestType"));
                        request.put("status", document.getString("status"));
                        Long requestNumberLong = document.getLong("requestNumber");
                        if (requestNumberLong != null) {
                            request.put("requestNumber", String.valueOf(requestNumberLong.intValue()));
                        } else {
                            request.put("requestNumber", "0");
                        }
                        closedRequests.add(request);
                    }


                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load approved/denied requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onComplete.run();
                });
    }



    private void populateRequests(List<Map<String, String>> requests, LinearLayout parentLayout, int defaultBackgroundColor) {
        parentLayout.removeAllViews();

        if (requests.isEmpty()) {
            TextView noRequestsMessage = new TextView(this);
            noRequestsMessage.setText("No requests available.");
            noRequestsMessage.setTextColor(getResources().getColor(android.R.color.white));
            noRequestsMessage.setTextSize(16);
            noRequestsMessage.setGravity(Gravity.CENTER);
            parentLayout.addView(noRequestsMessage);
            return;
        }

        int idCounter = 1;
        for (Map<String, String> request : requests) {
            String name = request.get("employeeName");
            String requestType = request.get("requestType");
            String requestNumberStr = request.get("requestNumber");
            String status = request.get("status");

            int requestNumber = 0;
            if (requestNumberStr != null && requestNumberStr.matches("\\d+")) {
                requestNumber = Integer.parseInt(requestNumberStr);
            }

            int backgroundColor = defaultBackgroundColor;
            if ("approved".equalsIgnoreCase(status)) {
                backgroundColor = getResources().getColor(android.R.color.holo_green_dark);
            } else if ("denied".equalsIgnoreCase(status)) {
                backgroundColor = getResources().getColor(android.R.color.holo_red_dark);
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
        requestLayout.setId(id);
        requestLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
        );
        layoutParams.setMargins(0, 8, 0, 8);
        requestLayout.setLayoutParams(layoutParams);

        requestLayout.setPadding(12, 12, 12, 12);
        requestLayout.setBackgroundColor(backgroundColor);

        TextView nameTextView = new TextView(this);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        nameTextView.setText(employeeEmail);
        nameTextView.setTextColor(getResources().getColor(android.R.color.white));
        nameTextView.setTextSize(16);
        nameTextView.setGravity(Gravity.CENTER_VERTICAL);

        TextView requestTypeTextView = new TextView(this);
        requestTypeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        requestTypeTextView.setText(requestType);
        requestTypeTextView.setTextColor(getResources().getColor(android.R.color.white));
        requestTypeTextView.setTextSize(16);
        requestTypeTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);

        requestLayout.addView(nameTextView);
        requestLayout.addView(requestTypeTextView);


        requestLayout.setOnClickListener(v -> {
            Intent intent;
            if ("Vacation".equalsIgnoreCase(requestType) || "Sick Day".equalsIgnoreCase(requestType)) {
                intent = new Intent(ManagerRequestPage.this, ManagerDialogRequestDetails.class);
            } else if ("shift change".equalsIgnoreCase(requestType)) {
                intent = new Intent(ManagerRequestPage.this, ManagerShiftChangeDialog.class);
            } else {
                    return;
                }

            intent.putExtra("managerEmail", managerEmail);
            intent.putExtra("employeeEmail", employeeEmail);
            intent.putExtra("requestNumber", number);
            startActivity(intent);
        });

        return requestLayout;
    }

    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() ==  R.id.m_home_page) {
            Toast.makeText(ManagerRequestPage.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerRequestPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerProfile.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerRequestPage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerRequestPage.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            Toast.makeText(ManagerRequestPage.this, "Published work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerWorkArrangement.class);
        } else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerRequestPage.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerRequestPage.this, "Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerRequestPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, Login.class);
        }

        if (intent != null) {
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}