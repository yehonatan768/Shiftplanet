package com.example.shiftplanet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EmployeeRequestPage extends AppCompatActivity {

    private EditText startDateEditText, endDateEditText, detailsEditText;
    private AutoCompleteTextView reasonDropdown;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_request_page);

        // Initialize views
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        reasonDropdown = findViewById(R.id.reason_dropdown);
        detailsEditText = findViewById(R.id.details);

        // Setup dropdown menu
        String[] reasons = {"Vacation", "Sick Leave"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, reasons);
        reasonDropdown.setAdapter(adapter);

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

        // Setup Submit Request button
        Button submitRequestButton = findViewById(R.id.submit_request_button);
        submitRequestButton.setOnClickListener(v -> {
            // Get values from inputs
            String reason = reasonDropdown.getText().toString();
            String startDate = startDateEditText.getText().toString();
            String endDate = endDateEditText.getText().toString();
            String details = detailsEditText.getText().toString();

            // Get current user's email
            String employeeEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            // Get the manager's email dynamically from Firestore based on employee email
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("email", employeeEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assume only one document for the current employee
                            String managerEmail = queryDocumentSnapshots.getDocuments().get(0).getString("manager's email");

                            // Now you can submit the request
                            submitRequest(reason, startDate, endDate, details, managerEmail);
                        } else {
                            Toast.makeText(EmployeeRequestPage.this, "Employee not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                        Toast.makeText(EmployeeRequestPage.this, "Error fetching manager's email", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    // Function to submit request to Firebase Firestore
    public void submitRequest(String reason, String startDate, String endDate, String details, String managerEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create request object
        Map<String, Object> request = new HashMap<>();
        request.put("reason", reason);
        request.put("startDate", startDate);
        request.put("endDate", endDate);
        request.put("details", details);
        request.put("status", "pending"); // Initial status is pending
        request.put("employeeEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail()); // Employee's email
        request.put("managerEmail", managerEmail); // Manager's email
        request.put("timestamp", FieldValue.serverTimestamp()); // Timestamp

        // Add request to Firestore
        db.collection("Requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    // Request successfully added
                    Toast.makeText(EmployeeRequestPage.this, "Request submitted", Toast.LENGTH_SHORT).show();
                    // Optionally send notification to manager
                    sendNotificationToManager(managerEmail);
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(EmployeeRequestPage.this, "Error submitting request", Toast.LENGTH_SHORT).show();
                });
    }

    // Function to send notification to manager
    private void sendNotificationToManager(String managerEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Find the manager by email
        db.collection("users")
                .whereEqualTo("email", managerEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Found manager, now send notification
                        String managerFCMToken = queryDocumentSnapshots.getDocuments().get(0).getString("fcmToken");
                        if (managerFCMToken != null) {
                            sendPushNotification(managerFCMToken, "New leave request", "You have a new leave request to approve.");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(EmployeeRequestPage.this, "Error finding manager", Toast.LENGTH_SHORT).show();
                });
    }

    // Function to send push notification (this can be customized based on your notification method)
    private void sendPushNotification(String fcmToken, String title, String message) {
        // FCM Server Key
        String serverKey = "BOSjbN8jTCk5nrgWdcQhBBcxwO5RTHa9oGK7N9-bkfaqCuCuL23BQ6BEtYvSXnGj7z-EfZwGBxAmjz3Uiaw8cSE"; // הוסף את ה-Server Key שלך כאן

        // URL של FCM
        String url = "https://fcm.googleapis.com/fcm/send";

        // JSON body של הבקשה
        JSONObject json = new JSONObject();
        try {
            json.put("to", fcmToken);
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", message);
            json.put("notification", notification);

            // יצירת קשר עם ה-HTTP Server של FCM
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "key=" + serverKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // לשלוח את הבקשה
            OutputStream os = connection.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            // קבלת תוצאה
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // הודעה נשלחה בהצלחה
                Log.d("FCM", "Notification sent successfully.");
            } else {
                // שגיאה בשליחת ההודעה
                Log.e("FCM", "Error sending notification: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        }else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeRequestPage.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
        }else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeRequestPage.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeRequestStatus.class);
        }else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeRequestPage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
        }else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeRequestPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, Login.class);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
        return true; // Return true to indicate that the item has been handled
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
