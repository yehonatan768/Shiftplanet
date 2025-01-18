package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class EmployeeRequestStatus extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_request_status);

        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout1);
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
    }
    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeRequestStatus.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeRequestStatus.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeRequestStatus.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeRequestStatus.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeRequestPage.class);
        }else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeRequestStatus.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
        }else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeRequestStatus.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeRequestStatus.class);
        }else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeRequestStatus.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, EmployeeHomePage.class);
        }else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeRequestStatus.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeRequestStatus.this, Login.class);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        startActivity(intent);
        finish();

    }
}