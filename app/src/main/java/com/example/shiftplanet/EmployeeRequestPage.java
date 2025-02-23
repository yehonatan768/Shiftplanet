package com.example.shiftplanet;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EmployeeRequestPage extends AppCompatActivity {

    private String[] requestTypes = {"Sick Day", "Vacation"};
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapterItems;
    private EditText startDateEditText, endDateEditText, detailsEditText;
    private String employeeEmail, managerEmail;
    private int businessCode;
    private FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private DrawerLayout drawerLayout; // הגדרה כמשתנה גלובלי
    private NavigationView navigationView;
    private Toolbar toolbar;
    private static final int PICK_DOCUMENT_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;
    private Uri documentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_request_page);
        initializeUI();

        // Retrieve the email
        String email = getIntent().getStringExtra("LOGIN_EMAIL");
        if (email != null) {
            employeeEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }

        // Setup date pickers
        startDateEditText.setOnClickListener(v -> showDatePicker(date -> startDateEditText.setText(date)));
        endDateEditText.setOnClickListener(v -> showDatePicker(date -> endDateEditText.setText(date)));



        // Setup dropdown
        setUpRequestTypeDropdown();

        // Setup Submit Request button
        Button submitRequestButton = findViewById(R.id.submit_request_button);
        submitRequestButton.setOnClickListener(v -> handleSubmitRequest());

        // Add document button click listener
        Button addDocumentButton = findViewById(R.id.add_document_button);
        addDocumentButton.setOnClickListener(v -> openDocumentPicker());
    }

    private void initializeUI() {
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        detailsEditText = findViewById(R.id.details);
        autoCompleteTextView = findViewById(R.id.autoCompleteRequestType);

        DrawerLayout drawerLayout = findViewById(R.id.employee_request_drawer_layout);
        NavigationView navigationView = findViewById(R.id.employee_request_nav_view);
        Toolbar toolbar = findViewById(R.id.employee_request_toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            handleNavigationItemSelected(menuItem);
            drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        });
    }

    private void setUpRequestTypeDropdown() {
        adapterItems = new ArrayAdapter<>(this, R.layout.request_type_list, requestTypes);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(EmployeeRequestPage.this, "Selected: " + item, LENGTH_SHORT).show();
        });
    }

    private void handleSubmitRequest() {
        String reason = autoCompleteTextView.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();
        String details = detailsEditText.getText().toString().trim();
        String employeeEmail = current.getEmail();

        if (!requestFieldsCheck(reason, startDate, endDate)) {
            Toast.makeText(this, "Please fill in all fields", LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (start.after(end)) {
                Toast.makeText(EmployeeRequestPage.this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch manager email and business code from Firestore
            fetchManagerDetailsAndSubmitRequest(reason, startDate, endDate, details, employeeEmail);

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(EmployeeRequestPage.this, "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchManagerDetailsAndSubmitRequest(String reason, String startDate, String endDate, String details, String employeeEmail) {
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
    }

    private void submitRequest(String reason, String startDate, String endDate, String details, String employeeEmail, String managerEmail) {
        getNextRequestNumber(requestNumber -> {
            if (requestNumber == -1) {
                Toast.makeText(EmployeeRequestPage.this, "Error generating request number", LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> request = new HashMap<>();
            request.put("reason", reason);
            request.put("startDate", startDate);
            request.put("endDate", endDate);
            request.put("details", details);
            request.put("status", "pending");
            request.put("employeeEmail", employeeEmail);
            request.put("managerEmail", managerEmail);
            request.put("businessCode", businessCode);
            request.put("timestamp", FieldValue.serverTimestamp());
            request.put("requestNumber", requestNumber);

            // Save request to Firestore
            db.collection("Requests").add(request)
                    .addOnSuccessListener(documentReference -> {
                        // After saving the request, send the notification to the manager
                        sendFCMNotificationToManager(managerEmail, reason, startDate, endDate);
                        Toast.makeText(EmployeeRequestPage.this, "Request submitted", LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(EmployeeRequestPage.this, "Error submitting request", LENGTH_SHORT).show());
        });
    }

    private void sendFCMNotificationToManager(String managerEmail, String reason, String startDate, String endDate) {
        // Fetch manager's FCM token from Firestore (or from another source if you have it saved elsewhere)
        db.collection("users").whereEqualTo("email", managerEmail).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String managerFCMToken = document.getString("fcmToken");

                        if (managerFCMToken != null) {
                            // Create the notification message
                            Map<String, String> notificationData = new HashMap<>();
                            notificationData.put("title", "New Leave Request");
                            notificationData.put("message", "Employee has requested a " + reason + " from " + startDate + " to " + endDate);

                            // Send FCM notification
                            sendPushNotification(managerFCMToken, notificationData);
                        }
                    } else {
                        Log.e("FCM", "No manager found with email: " + managerEmail);
                    }
                });
    }

    private void sendPushNotification(String fcmToken, Map<String, String> notificationData) {
        // You can use Firebase Cloud Messaging (FCM) HTTP API to send the push notification.
        // You need to set up a server to make this HTTP request.

        // Here's an example of how to send a push notification using FCM's HTTP API:

        String url = "https://fcm.googleapis.com/fcm/send";
        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject notification = new JSONObject();
            notification.put("title", notificationData.get("title"));
            notification.put("body", notificationData.get("message"));

            jsonBody.put("to", fcmToken); // The target FCM token
            jsonBody.put("notification", notification);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Log.d("FCM", "Notification sent successfully: " + response.toString());
                    },
                    error -> {
                        Log.e("FCM", "Error sending notification: " + error.getMessage());
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "key=BOSjbN8jTCk5nrgWdcQhBBcxwO5RTHa9oGK7N9-bkfaqCuCuL23BQ6BEtYvSXnGj7z-EfZwGBxAmjz3Uiaw8cSE"); // Replace with your FCM server key
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(EmployeeRequestPage.this);
            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getNextRequestNumber(OnRequestNumberGeneratedListener listener) {
        db.collection("RequestCounters").document("RequestsCounter")
                .update("counter", FieldValue.increment(1))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("RequestCounters").document("GlobalCounter")
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        long counter = documentSnapshot.getLong("counter");
                                        listener.onRequestNumberGenerated(counter);
                                    } else {
                                        listener.onRequestNumberGenerated(-1);
                                    }
                                });
                    } else {
                        listener.onRequestNumberGenerated(-1);
                    }
                });
    }

    private boolean requestFieldsCheck(String reason, String startDate, String endDate) {
        return !reason.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty();
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Pick any file type
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST);
    }

    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeRequestPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeRequestPage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeRequestPage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeRequestPage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeRequestPage.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeShiftChange.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeRequestPage.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeRequestPage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeRequestPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
    }



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

    interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    interface OnRequestNumberGeneratedListener {
        void onRequestNumberGenerated(long requestNumber);
    }
}
