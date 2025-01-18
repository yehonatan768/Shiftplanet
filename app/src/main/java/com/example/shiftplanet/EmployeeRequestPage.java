package com.example.shiftplanet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class EmployeeRequestPage extends AppCompatActivity {

    private EditText startDateEditText, endDateEditText;
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

        // Setup Drawer Toggle (הכפתור לפתיחת התפריט)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup NavigationView listener (לטפל בלחיצות על פריטי התפריט)
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            handleNavigationItemSelected(menuItem);
            drawerLayout.closeDrawer(Gravity.LEFT);
            return true;
        });
    }

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
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();
        return true; // מחזיר true כי הטיפול ב-item הושלם
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
}
