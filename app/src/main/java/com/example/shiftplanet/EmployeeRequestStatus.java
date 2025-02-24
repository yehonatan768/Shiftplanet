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

        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            employeeEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }

        fetchRequests(() -> {

            setContentView(R.layout.employee_request_status);
            initializeUI();
        });
    }
    private void initializeUI() {

        drawerLayout = findViewById(R.id.drawer_layout1);
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
                .whereEqualTo("employeeEmail", employeeEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingRequests.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("requestType", document.getString("reason"));
                        request.put("requestStatus", document.getString("status"));
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
                .whereEqualTo("employeeEmail", employeeEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    closedRequests.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, String> request = new HashMap<>();
                        request.put("requestType", document.getString("reason"));
                        request.put("status", document.getString("status"));
                        closedRequests.add(request);
                    }

                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load approved/denied requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onComplete.run();
                });
    }


    private void populateRequests(List<Map<String, String>> requests, LinearLayout parentLayout, int backgroundColor) {

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
        nameTextView.setText(name);
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

        return requestLayout;
    }


    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_home_page) {
            Toast.makeText(EmployeeRequestStatus.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeRequestStatus.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeProfile.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeRequestStatus.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeWorkArrangement.class);
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
            intent = new Intent(EmployeeRequestStatus.this, EmployeeShiftChangeRequest.class);
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