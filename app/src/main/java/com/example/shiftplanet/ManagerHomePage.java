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

public class ManagerHomePage extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // אפשרות לתמיכה בתצוגה בקצוות
        setContentView(R.layout.manager_home_page);

        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.manager_home_page);
        navigationView = findViewById(R.id.nav_view1);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }


    @Override

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // טיפול בפריטי התפריט הראשיים
        if (item.getItemId() == R.id.m_my_profile) {
            // פעולה עבור "הפרופיל שלי"
            Toast.makeText(ManagerHomePage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.employees_requests) {
            // פעולה עבור "בקשות עובדים"
            Toast.makeText(ManagerHomePage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.m_work_arrangement) {
            // פעולה עבור "סידור עבודה" (תת-תפריט ייפתח אוטומטית)
            Toast.makeText(ManagerHomePage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            // פעולה עבור "בניית סידור עבודה"
            Toast.makeText(ManagerHomePage.this, "Building work arrangement clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            // פעולה עבור "סידור עבודה שפורסם"
            Toast.makeText(ManagerHomePage.this, "Published work arrangement clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.m_notification) {
            // פעולה עבור "עדכונים לעובדים"
            Toast.makeText(ManagerHomePage.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.send_notifications) {
            // פעולה עבור "שליחת עדכון"
            Toast.makeText(ManagerHomePage.this, "Send notification clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.sent_notifications) {
            // פעולה עבור "עדכונים שנשלחו"
            Toast.makeText(ManagerHomePage.this, "Sent notifications clicked", Toast.LENGTH_SHORT).show();
        }

        // סוגר את ה-Drawer
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
