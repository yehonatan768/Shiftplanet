package com.example.shiftplanet;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
    private String employeeEmail;

    private static final int PICK_DOCUMENT_REQUEST = 1; // constant for the file
    private Uri documentUri; // Variable that will hold the URI of the chosen file
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference(); // קישור לאחסון ב-Firebase

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
        setContentView(R.layout.employee_request_page);
        initializeUI();

        // Setup date pickers
        startDateEditText.setOnClickListener(v -> showDatePicker((date) -> startDateEditText.setText(date)));
        endDateEditText.setOnClickListener(v -> showDatePicker((date) -> endDateEditText.setText(date)));

        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.employee_request_page);
        navigationView = findViewById(R.id.employee_request_nav_view);
        toolbar = findViewById(R.id.employee_request_toolbar);

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


            if (!requestFieldsCheck(reason,startDate,endDate)) {
                Toast.makeText(this, "please fill in all fields", LENGTH_SHORT).show();
                return;
            }


            // המרת התאריכים למבני Date להשוואה
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {

                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);

                // בדיקה אם תאריך התחלה מאוחר יותר מתאריך הסיום
                if (start.after(end)) {
                    Toast.makeText(EmployeeRequestPage.this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show();
                    return;
                }

                // אם התאריכים תקינים, תוכל להמשיך לשלב הבא
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
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(EmployeeRequestPage.this, "Invalid date format", Toast.LENGTH_SHORT).show();
            }


    });

        // Add document button click listener
        Button addDocumentButton = findViewById(R.id.add_document_button);
        addDocumentButton.setOnClickListener(v -> openDocumentPicker());
    }

    private void initializeUI() {
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        detailsEditText = findViewById(R.id.details);
        autoCompleteTextView = findViewById(R.id.autoCompleteRequestType);
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Pick any file type
        startActivityForResult(intent, PICK_DOCUMENT_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_DOCUMENT_REQUEST) {
            if (data != null) {
                documentUri = data.getData(); // Save URI of selected document
                Toast.makeText(this, "Document selected: " + documentUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadDocumentToFirebase() {
        if (documentUri != null) {
            StorageReference fileReference = storageReference.child("documents/" + System.currentTimeMillis() + ".pdf");
            fileReference.putFile(documentUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String documentUrl = uri.toString();
                            saveRequestWithDocumentUrl(documentUrl);
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload document", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No document selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRequestWithDocumentUrl(String documentUrl) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "day off");
        request.put("reason", autoCompleteTextView.getText().toString().trim());
        request.put("startDate", startDateEditText.getText().toString().trim());
        request.put("endDate", endDateEditText.getText().toString().trim());
        request.put("details", detailsEditText.getText().toString().trim());
        request.put("status", "pending");
        request.put("employeeEmail", current.getEmail());
        request.put("managerEmail", managerEmail);
        request.put("businessCode", businessCode);
        request.put("timestamp", FieldValue.serverTimestamp());
        request.put("documentUrl", documentUrl);


        db.collection("Requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(EmployeeRequestPage.this, "Request submitted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeRequestPage.this, "Error submitting request", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitRequest(String reason, String startDate, String endDate, String details, String employeeEmail, String managerEmail) {
        getNextRequestNumber(requestNumber -> {
            if (requestNumber == -1) {
                Toast.makeText(EmployeeRequestPage.this, "Error generating request number", Toast.LENGTH_SHORT).show();
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

            db.collection("Requests")
                    .add(request)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(EmployeeRequestPage.this, "Request submitted with number: " + requestNumber, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EmployeeRequestPage.this, "Error submitting request", Toast.LENGTH_SHORT).show();
                    });
        });
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

    interface OnRequestNumberGeneratedListener {
        void onRequestNumberGenerated(long requestNumber);
    }

    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeRequestPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestPage.this, Profile.class);
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
            intent = new Intent(EmployeeRequestPage.this, EmployeeShiftChangeRequest.class);
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

    static boolean requestFieldsCheck(String reason, String startDate, String endDate ) {
        if (reason.isEmpty()) {
            return false;
        }
        if (startDate.isEmpty()) {
            return false;
        }
        if (endDate.isEmpty()) {
            return false;
        }
        else{
            return true;
        }
    }

}
