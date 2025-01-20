package com.example.shiftplanet;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import com.example.shiftplanet.databinding.EmployeeRequestPageBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EmployeeRequestPage extends AppCompatActivity {
    String[] requestTypes = {"Sick Day", "Vacation"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    private EditText startDateEditText, endDateEditText, detailsEditText;
    private AutoCompleteTextView reasonDropdown;
    private String managerEmail;
    private int businessCode;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_request_page);
        initializeUI();

        // Setup date pickers
        startDateEditText.setOnClickListener(v -> showDatePicker((date) -> startDateEditText.setText(date)));
        endDateEditText.setOnClickListener(v -> showDatePicker((date) -> endDateEditText.setText(date)));

        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.employee_request_page);
        navigationView = findViewById(R.id.nav_view);
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

        // Setup dropdown
        adapterItems = new ArrayAdapter<>(this, R.layout.request_type_list, requestTypes);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(EmployeeRequestPage.this, "Selected: " + item, LENGTH_SHORT).show();
        });

        // Setup Submit Request button
        Button submitRequestButton = findViewById(R.id.submit_request_button);
        submitRequestButton.setOnClickListener(v -> {
            String reason = autoCompleteTextView.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();
            String details = detailsEditText.getText().toString().trim();
            String employeeEmail = current.getEmail();

            if (reason.isEmpty()) {
                Toast.makeText(EmployeeRequestPage.this, "Please fill Request Type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate.isEmpty()) {
                Toast.makeText(EmployeeRequestPage.this, "Please fill Start Date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endDate.isEmpty()) {
                Toast.makeText(EmployeeRequestPage.this, "Please fill End Date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reason.equals("Vacation") && details.isEmpty()) {
                Toast.makeText(EmployeeRequestPage.this, "Please fill Details about the request", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the manager's email and business code from Firestore
            db.collection("users").document(current.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        managerEmail = document.getString("managerEmail");
                        businessCode = Integer.parseInt(document.getString("businessCode"));
                        submitRequest(reason, startDate, endDate, details, employeeEmail, managerEmail);
                    } else {
                        Log.e("FirestoreError", "Failed to fetch manager's email", task.getException());
                        Toast.makeText(EmployeeRequestPage.this, "Failed to retrieve manager's email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("FirestoreError", "Error getting user document", task.getException());
                }
            });
        });
    }

    // Function to submit request to Firebase Firestore
    public void submitRequest(String reason, String startDate, String endDate, String details, String employeeEmail, String managerEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create request object
        Map<String, Object> request = new HashMap<>();
        request.put("reason", reason);
        request.put("startDate", startDate);
        request.put("endDate", endDate);
        request.put("details", details);
        request.put("status", "pending"); // Initial status is pending
        request.put("employeeEmail", employeeEmail); // Employee's email
        request.put("managerEmail", managerEmail);
        request.put("businessCode", businessCode);
        request.put("timestamp", FieldValue.serverTimestamp()); // Timestamp

        // Add request to Firestore
        db.collection("Requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    // Request successfully added
                    Toast.makeText(EmployeeRequestPage.this, "Request submitted", Toast.LENGTH_SHORT).show();
                    // Optionally send notification to manager
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(EmployeeRequestPage.this, "Error submitting request", Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeUI() {
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        detailsEditText = findViewById(R.id.details);
        autoCompleteTextView = findViewById(R.id.autoCompleteRequestType);
    }

    // Function to handle navigation item clicks
    private boolean handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeRequestPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeRequestPage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeRequestPage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeRequestPage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeRequestPage.class);
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeRequestPage.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeRequestPage.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeRequestStatus.class);
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeRequestPage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeRequestPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, Login.class);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
        return true;
    }

    // Function to show date picker
    private void showDatePicker(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            listener.onDateSelected(date);
        }, year, month, day).show();
    }

    // Interface for date selection listener
    interface OnDateSelectedListener {
        void onDateSelected(String date);
    }
}
