package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EmployeeHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_home_page);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu from the XML file
        getMenuInflater().inflate(R.menu.employee_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handling item clicks
        if (item.getItemId() == R.id.constraints) {
            Toast.makeText(this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(this, "Days off clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(this, "Notification clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(this, "requests_status clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(this, "my profile clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
