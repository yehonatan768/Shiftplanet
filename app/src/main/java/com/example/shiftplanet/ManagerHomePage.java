package com.example.shiftplanet;
import android.content.Intent;
import android.os.Bundle;

import android.view.Gravity;
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
        Intent intent = null;
        if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerHomePage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
        }else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerHomePage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerHomePage.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            Toast.makeText(ManagerHomePage.this, "Published work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerHomePage.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerHomePage.this, "\"Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, ManagerHomePage.class);
        }else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerHomePage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerHomePage.this, Login.class);
        }
        // הוספת שאר האפשרויות בתפריט
        drawerLayout.closeDrawer(Gravity.LEFT);
        startActivity(intent);
        return true;
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
