package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class ManagerRequestPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_request_page);

        // Setup DrawerLayout and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
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

    private boolean handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerRequestPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
        }else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerRequestPage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerRequestPage.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            Toast.makeText(ManagerRequestPage.this, "Published work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerRequestPage.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerRequestPage.this, "\"Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, ManagerHomePage.class);
        }else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerRequestPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerRequestPage.this, Login.class);
        }
        // הוספת שאר האפשרויות בתפריט
        drawerLayout.closeDrawer(Gravity.LEFT);
        startActivity(intent);
        return true;
    }
}
