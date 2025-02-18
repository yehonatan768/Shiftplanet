package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ManagerWorkArrangement extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String managerEmail;
    LinearLayout morningShiftLayout, eveningShiftLayout;
    int numShifts = 3;
    TextView firstDayLetter, firstDayNumber, secondDayLetter, secondDayNumber, thirdDayLetter, thirdDayNumber, calendarTitle;
    ImageButton btnPreviousWeek, btnNextWeek, btnPreviousDay, btnNextDay;
    Calendar currentWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manager_work_arrangement_page);

        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.manager_work_arrangement);
        navigationView = findViewById(R.id.nav_view1);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        morningShiftLayout = findViewById(R.id.morningRow1);
        eveningShiftLayout = findViewById(R.id.eveningRow1);

        firstDayLetter = findViewById(R.id.first_day_letter);
        firstDayNumber = findViewById(R.id.first_day_number);
        secondDayLetter = findViewById(R.id.second_day_letter);
        secondDayNumber = findViewById(R.id.second_day_number);
        thirdDayLetter = findViewById(R.id.third_day_letter);
        thirdDayNumber = findViewById(R.id.third_day_number);

        createDynamicShifts(morningShiftLayout, "Morning");
        createDynamicShifts(eveningShiftLayout, "Evening");

        calendarTitle = findViewById(R.id.calendar_title);

        btnPreviousWeek = findViewById(R.id.btn_previous_week);
        btnNextWeek = findViewById(R.id.btn_next_week);
        btnPreviousDay = findViewById(R.id.btn_previous_day_set);
        btnNextDay = findViewById(R.id.btn_next_day_set);

        currentWeek = Calendar.getInstance();
        updateCalendarTitleAndDates();

        btnPreviousWeek.setOnClickListener(view -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, -1);
            updateCalendarTitleAndDates();
        });

        btnNextWeek.setOnClickListener(view -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, 1);
            updateCalendarTitleAndDates();
        });

        btnPreviousDay.setOnClickListener(view -> {
            currentWeek.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            checkAndUpdateWeek();
        });

        btnNextDay.setOnClickListener(view -> {
            currentWeek.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            checkAndUpdateWeek();
        });
    }

    private void updateCalendarTitleAndDates() {
        SimpleDateFormat format = new SimpleDateFormat("d/M", Locale.getDefault());
        Calendar sunday = (Calendar) currentWeek.clone();
        sunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendarTitle.setText("Work Arrangement - " + format.format(sunday.getTime()));

        updateDateDisplay();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        Calendar calendar = (Calendar) currentWeek.clone();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        firstDayLetter.setText(dayFormat.format(calendar.getTime()));
        firstDayNumber.setText(dateFormat.format(calendar.getTime()));

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        secondDayLetter.setText(dayFormat.format(calendar.getTime()));
        secondDayNumber.setText(dateFormat.format(calendar.getTime()));

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        thirdDayLetter.setText(dayFormat.format(calendar.getTime()));
        thirdDayNumber.setText(dateFormat.format(calendar.getTime()));
    }

    private void checkAndUpdateWeek() {
        Calendar temp = (Calendar) currentWeek.clone();
        temp.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        Calendar tempSaturday = (Calendar) currentWeek.clone();
        tempSaturday.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

        if (currentWeek.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || currentWeek.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            updateCalendarTitleAndDates();
        }
    }

    private void createDynamicShifts(LinearLayout parentLayout, String shiftType) {
        parentLayout.removeAllViews();
        parentLayout.setWeightSum(numShifts);

        for (int i = 0; i < numShifts; i++) {
            LinearLayout shiftContainer = new LinearLayout(this);
            shiftContainer.setLayoutParams(new LinearLayout.LayoutParams(0, 160, 1));
            shiftContainer.setGravity(Gravity.CENTER);
            shiftContainer.setPadding(8, 8, 8, 8);
            shiftContainer.setBackgroundResource(R.drawable.shift_background);
            shiftContainer.setOrientation(LinearLayout.VERTICAL);

            ImageButton addButton = new ImageButton(this);
            addButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            addButton.setImageResource(R.drawable.ic_add);
            addButton.setBackgroundResource(android.R.color.transparent);
            addButton.setContentDescription("Add Shift");

            final int shiftIndex = i;
            addButton.setOnClickListener(view -> onAddShiftClicked(shiftIndex, shiftType));

            shiftContainer.addView(addButton);
            parentLayout.addView(shiftContainer);
        }
    }

    private void onAddShiftClicked(int shiftIndex, String shiftType) {
        Toast.makeText(this, "Adding shift " + (shiftIndex + 1) + " for " + shiftType, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        String message = "";

        if (item.getItemId() == R.id.m_my_profile) {
            message = "Already on My Profile";
            intent = new Intent(this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            message = "Employees requests clicked";
            intent = new Intent(this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            message = "Build work arrangement clicked";
        } else if (item.getItemId() == R.id.published_work_arrangement) {
            message = "Published work arrangement clicked";
        } else if (item.getItemId() == R.id.send_notifications) {
            message = "Send notifications clicked";
            intent = new Intent(this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            message = "Sent notifications clicked";
            intent = new Intent(this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            message = "Logging out...";
            intent = new Intent(this, Login.class);
        }

        // Show the Toast and delay navigation
        if (!message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        if (intent != null) {
            showToastThenNavigate(message, intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showToastThenNavigate(String message, Intent intent) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        intent.putExtra("LOGIN_EMAIL", managerEmail);

        new android.os.Handler().postDelayed(() -> {
            startActivity(intent);
            // Don't finish the current activity immediately
        }, 500);
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