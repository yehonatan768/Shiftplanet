package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class EmployeeSubmitConstraintsPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseFirestore db;
    private String employeeEmail;
    private String managerEmail;
    private int businessCode;
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the email
        employeeEmail = getIntent().getStringExtra("LOGIN_EMAIL");

        if (employeeEmail != null) {
            Log.d(TAG, "Received email: " + employeeEmail);
        } else {
            Log.e(TAG, "No email received");
        }
        setContentView(R.layout.employee_submit_constraints_page);

        db = FirebaseFirestore.getInstance();
        // Set Toolbar as ActionBar
        toolbar = findViewById(R.id.submit_constraints_toolbar);
        setSupportActionBar(toolbar);

        // Set up Navigation Drawer
        drawerLayout = findViewById(R.id.submit_constraints);
        navigationView = findViewById(R.id.submit_constraints_nav_view);
        navigationView.setNavigationItemSelectedListener(this);  // Use 'this' since the activity implements the listener

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize the calendar
        Calendar calendar = Calendar.getInstance();

        // Add one week to the current date
        calendar.add(Calendar.WEEK_OF_YEAR, 1);

        // Format to show date in the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Set the dates for the next week
        setDateForDay(R.id.sundayDate, calendar, dateFormat);
        setDateForDay(R.id.mondayDate, calendar, dateFormat);
        setDateForDay(R.id.tuesdayDate, calendar, dateFormat);
        setDateForDay(R.id.wednesdayDate, calendar, dateFormat);
        setDateForDay(R.id.thursdayDate, calendar, dateFormat);
        setDateForDay(R.id.fridayDate, calendar, dateFormat);


        // Collecting user's choices (your existing CheckBox code)
        CheckBox sundayMorning = findViewById(R.id.sundayMorning);
        CheckBox sundayEvening = findViewById(R.id.sundayEvening);
        CheckBox mondayMorning = findViewById(R.id.mondayMorning);
        CheckBox mondayEvening = findViewById(R.id.mondayEvening);
        CheckBox tuesdayMorning = findViewById(R.id.tuesdayMorning);
        CheckBox tuesdayEvening = findViewById(R.id.tuesdayEvening);
        CheckBox wednesdayMorning = findViewById(R.id.wednesdayMorning);
        CheckBox wednesdayEvening = findViewById(R.id.wednesdayEvening);
        CheckBox thursdayMorning = findViewById(R.id.thursdayMorning);
        CheckBox thursdayEvening = findViewById(R.id.thursdayEvening);
        CheckBox fridayMorning = findViewById(R.id.fridayMorning);
        CheckBox fridayEvening = findViewById(R.id.fridayEvening);

        Button submitConstraintsButton = findViewById(R.id.submit_constraints_btn);
        TextInputEditText notesEditText = findViewById(R.id.notes);
        submitConstraintsButton.setOnClickListener(v -> {
            boolean sundayMorningSelected = sundayMorning.isChecked();
            boolean sundayEveningSelected = sundayEvening.isChecked();
            boolean mondayMorningSelected = mondayMorning.isChecked();
            boolean mondayEveningSelected = mondayEvening.isChecked();
            boolean tuesdayMorningSelected = tuesdayMorning.isChecked();
            boolean tuesdayEveningSelected = tuesdayEvening.isChecked();
            boolean wednesdayMorningSelected = wednesdayMorning.isChecked();
            boolean wednesdayEveningSelected = wednesdayEvening.isChecked();
            boolean thursdayMorningSelected = thursdayMorning.isChecked();
            boolean thursdayEveningSelected = thursdayEvening.isChecked();
            boolean fridayMorningSelected = fridayMorning.isChecked();
            boolean fridayEveningSelected = fridayEvening.isChecked();
            String notes = notesEditText.getText().toString();

            db.collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        managerEmail = document.getString("managerEmail");
                        businessCode = Integer.parseInt(document.getString("businessCode"));
                        submitConstraints(sundayMorningSelected, sundayEveningSelected, mondayMorningSelected, mondayEveningSelected,
                                tuesdayMorningSelected, tuesdayEveningSelected, wednesdayMorningSelected, wednesdayEveningSelected,
                                thursdayMorningSelected, thursdayEveningSelected, fridayMorningSelected, fridayEveningSelected,
                                managerEmail, businessCode,notes);
                    } else {
                        Log.e("FirestoreError", "Failed to fetch manager's email", task.getException());
                        Toast.makeText(EmployeeSubmitConstraintsPage.this, "Failed to retrieve manager's email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("FirestoreError", "Error getting user document", task.getException());
                }
            });
        });
    }

    // Submit constraints to Firestore
    private void submitConstraints(boolean sundayMSelected, boolean sundayESelected, boolean mondayMSelected, boolean mondayESelected,
                                   boolean tuesdayMSelected, boolean tuesdayESelected, boolean wednesdayMSelected, boolean wednesdayESelected,
                                   boolean thursdayMSelected, boolean thursdayESelected, boolean fridayMSelected, boolean fridayESelected,
                                   String managerEmail, int businessCode, String notes) {

        // Create a unique ID for the new request (e.g., using UUID or timestamp)
        String requestId = UUID.randomUUID().toString(); // Use UUID to generate a unique ID for each request

        // Creating the object to send to Firestore
        Map<String, Object> userConstraints = new HashMap<>();
        userConstraints.put("sundayMorning", sundayMSelected);
        userConstraints.put("sundayEvening", sundayESelected);
        userConstraints.put("mondayMorning", mondayMSelected);
        userConstraints.put("mondayEvening", mondayESelected);
        userConstraints.put("tuesdayMorning", tuesdayMSelected);
        userConstraints.put("tuesdayEvening", tuesdayESelected);
        userConstraints.put("wednesdayMorning", wednesdayMSelected);
        userConstraints.put("wednesdayEvening", wednesdayESelected);
        userConstraints.put("thursdayMorning", thursdayMSelected);
        userConstraints.put("thursdayEvening", thursdayESelected);
        userConstraints.put("fridayMorning", fridayMSelected);
        userConstraints.put("fridayEvening", fridayESelected);
        userConstraints.put("businessCode", businessCode);
        userConstraints.put("employeeEmail", employeeEmail);
        userConstraints.put("managerEmail", managerEmail);
        userConstraints.put("notes", notes);
        userConstraints.put("timestamp", FieldValue.serverTimestamp());

        // Send constraints to Firestore with a unique document ID (using the generated requestId)
        db.collection("Availability") // Use a unique ID for each request
                .add(userConstraints) // Add the constraints as a new document
                .addOnSuccessListener(documentReference -> {
                    // Success
                    Toast.makeText(EmployeeSubmitConstraintsPage.this, "Constraints submitted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failure
                    Toast.makeText(EmployeeSubmitConstraintsPage.this, "Error submitting constraints", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to set the date for each day
    private void setDateForDay(int textViewId, Calendar calendar, SimpleDateFormat dateFormat) {
        // Clone the calendar and add days
        Calendar dayCalendar = (Calendar) calendar.clone();
        int offset = getDayOffset(textViewId);
        dayCalendar.add(Calendar.DAY_OF_YEAR, offset);

        // Format the date and set it in the TextView
        String formattedDate = dateFormat.format(dayCalendar.getTime());
        TextView dayDateTextView = findViewById(textViewId);
        dayDateTextView.setText(formattedDate);
    }

    // Return the offset for the given day TextView ID
    private int getDayOffset(int textViewId) {
        if (textViewId == R.id.sundayDate) {
            return 0;
        } else if (textViewId == R.id.mondayDate) {
            return 1;
        } else if (textViewId == R.id.tuesdayDate) {
            return 2;
        } else if (textViewId == R.id.wednesdayDate) {
            return 3;
        } else if (textViewId == R.id.thursdayDate) {
            return 4;
        } else if (textViewId == R.id.fridayDate) {
            return 5;
        }
        return -1;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, EmployeeShiftChange.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeSubmitConstraintsPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeSubmitConstraintsPage.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
        return true; // Return true as the item was handled
    }
}
