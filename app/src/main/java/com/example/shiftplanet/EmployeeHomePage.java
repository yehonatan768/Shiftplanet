package com.example.shiftplanet;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class EmployeeHomePage extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // אם התפריט נבחר, נרצה שה-ActionBarDrawerToggle יטפל בו
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // אפשרות לתמיכה בתצוגה בקצוות
        setContentView(R.layout.employee_home_page);

        // אתחול רכיבי ה-UI
        drawerLayout = findViewById(R.id.employee_home_page);
        navigationView = findViewById(R.id.nav_view);

        // אתחול ה-ActionBarDrawerToggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // הצגת כפתור ההhamburger
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // הוספת Listner לתפריט הניווט
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.e_my_profile) {
                    Toast.makeText(EmployeeHomePage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.work_arrangement) {
                    Toast.makeText(EmployeeHomePage.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.constraints) {
                    Toast.makeText(EmployeeHomePage.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.day_off) {
                    Toast.makeText(EmployeeHomePage.this, "Day off clicked", Toast.LENGTH_SHORT).show();
                }
                return true; // מחזיר true כי הטיפול ב-item הושלם
            }
        });
    }

    @Override
    public void onBackPressed() {
        // אם הDrawer פתוח, נסגור אותו
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // אם הDrawer סגור, נקרא לפונקציה הבסיסית של backPressed
            super.onBackPressed();
        }
    }
}
