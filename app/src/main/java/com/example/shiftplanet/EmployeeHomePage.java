package com.example.shiftplanet;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EmployeeHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.employee_home_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manager_home_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating the menu from the XML file
        getMenuInflater().inflate(R.menu.employee_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handling item clicks
        if (item.getItemId() == R.id.constraints) {
            Toast.makeText(this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            //Intent intent = new Intent(EHomePage.this, Constraints.class);
            //startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(this, "Days off clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(this, "Notification clicked", Toast.LENGTH_SHORT).show();
            return true;
        }else if (item.getItemId() == R.id.shiftchange) {
            Toast.makeText(this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
            else if (item.getItemId() == R.id.e_workarrangment) {
            Toast.makeText(this, "Work arrangment clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (item.getItemId() == R.id.settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}