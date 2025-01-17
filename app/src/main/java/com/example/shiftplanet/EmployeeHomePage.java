package com.example.shiftplanet;
import android.os.Bundle;

import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class EmployeeHomePage extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.employee_home_page);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.employee_home_page);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeHomePage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeHomePage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeHomePage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeHomePage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true; // מחזיר true כי הטיפול ב-item הושלם

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
