package com.example.shiftplanet;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeShiftChangeRequest extends AppCompatActivity {

    private EditText DateEditText, HoursEditText, detailsEditText;
    private String managerEmail;
    private int businessCode;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String employeeEmail;
    private String employeeName;


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
        setContentView(R.layout.employee_shiftchange_request);
        initializeUI();
        initToolbar();
        fetchEmployeeName();



        DateEditText.setOnClickListener(v -> showDatePicker((date) -> DateEditText.setText(date)));


        Button submitShiftChangeButton = findViewById(R.id.submit_shiftchange_button);
        submitShiftChangeButton.setOnClickListener(v -> {
            String Date = DateEditText.getText().toString().trim();
            String Hours = HoursEditText.getText().toString().trim();
            String details = detailsEditText.getText().toString().trim();
            String employeeEmail = current.getEmail();

            if (!requestFieldsCheck(Date, Hours)) {
                Toast.makeText(this, "please fill in all fields", LENGTH_SHORT).show();
                return;
            }



            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date date = sdf.parse(Date);



                db.collection("users").document(current.getUid()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            managerEmail = document.getString("managerEmail");
                            businessCode = Integer.parseInt(document.getString("businessCode"));
                            submitShiftChange(Date, Hours, details, employeeEmail, managerEmail);
                        } else {
                            Log.e("FirestoreError", "Failed to fetch manager's email", task.getException());
                            Toast.makeText(EmployeeShiftChangeRequest.this, "Failed to retrieve manager's email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting user document", task.getException());
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(EmployeeShiftChangeRequest.this, "Invalid date format", Toast.LENGTH_SHORT).show();
            }


        });

    }

    private void initializeUI() {
        DateEditText = findViewById(R.id.date);
        HoursEditText = findViewById(R.id.hours);
        detailsEditText = findViewById(R.id.details);
    }

    private void initToolbar() {

        drawerLayout = findViewById(R.id.employee_shiftchange_page);
        navigationView = findViewById(R.id.employee_shiftchange_nav_view);
        toolbar = findViewById(R.id.employee_shiftchange_toolbar);


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
    }

    private void fetchEmployeeName() {
        db.collection("users").document(current.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        employeeName = documentSnapshot.getString("fullname");
                        Log.d(TAG, "Employee name retrieved: " + employeeName);
                    } else {
                        Log.e(TAG, "Employee document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching employee name", e);
                });
    }


    private void submitShiftChange(String Date, String hours, String details, String employeeEmail, String managerEmail) {
        getNextRequestNumber(requestNumber -> {
            if (requestNumber == -1) {
                Toast.makeText(EmployeeShiftChangeRequest.this, "Error generating request number", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> request = new HashMap<>();
            request.put("Date", Date);
            request.put("Hours", hours);
            request.put("details", details);
            request.put("status", "pending");
            request.put("approvedByEmployee", false);
            request.put("employeeName", employeeName);
            request.put("employeeEmail", employeeEmail);
            request.put("managerEmail", managerEmail);
            request.put("businessCode", businessCode);
            request.put("timestamp", FieldValue.serverTimestamp());
            request.put("requestNumber", requestNumber);

            db.collection("ShiftChangeRequests")
                    .add(request)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(EmployeeShiftChangeRequest.this, "Request submitted with number: " + requestNumber, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EmployeeShiftChangeRequest.this, "Error submitting request", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void getNextRequestNumber(OnRequestNumberGeneratedListener listener) {
        db.collection("ShiftChangeCounterDB").document("shiftChangeCounter")
                .update("counter", FieldValue.increment(1))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        db.collection("ShiftChangeCounterDB").document("shiftChangeCounter")
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
        if (item.getItemId() == R.id.e_home_page) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_my_profile) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeProfile.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeWorkArrangement.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.shift_change) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeShiftChangeRequest.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.requests_status) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.notification) {
            intent = new Intent(EmployeeShiftChangeRequest.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeShiftChangeRequest.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeShiftChangeRequest.this, Login.class);
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

    static boolean requestFieldsCheck(String Date,String hours ) {
        if (Date.isEmpty()) {
            return false;
        }
        if (hours.isEmpty()) {
            return false;
        }
        else{
            return true;
        }
    }











}
