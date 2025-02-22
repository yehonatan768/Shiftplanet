package com.example.shiftplanet;

import com.example.shiftplanet.utils.WorkSchedule;
import com.example.shiftplanet.dialogs.ShiftDialogFragment;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManagerWorkArrangement extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String managerEmail;
    LinearLayout morningShiftLayout, eveningShiftLayout;
    TextView firstDayLetter, firstDayNumber, secondDayLetter, secondDayNumber, thirdDayLetter, thirdDayNumber, calendarTitle;
    ImageButton btnPreviousWeek, btnNextWeek, btnPreviousDay, btnNextDay;
    Calendar currentWeek;
    private String workArrangementId;
    private WorkSchedule workSchedule;
    protected FirebaseFirestore db;
    private static final String TAG = "ShiftDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.manager_work_arrangement_page);

        managerEmail = getIntent().getStringExtra("LOGIN_EMAIL");
        if (managerEmail == null) {
            Toast.makeText(this, "Error: Manager email is not set.", Toast.LENGTH_SHORT).show();
            return;
        }

        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        db = FirebaseFirestore.getInstance();
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
        calendarTitle = findViewById(R.id.calendar_title);

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

        // Always use Sunday of the current week
        Calendar sunday = (Calendar) currentWeek.clone();
        sunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String formattedDate = format.format(sunday.getTime());

        calendarTitle.setText("Work Arrangement - " + formattedDate);

        updateDateDisplay();
        getWorkArrangement(); // 🔥 Ensure Firestore data is fetched and UI updates
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
        updateShiftsOnUI();
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

    private void getWorkArrangement() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M", Locale.getDefault());

        // Always use Sunday of the current week
        Calendar sunday = (Calendar) currentWeek.clone();
        sunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String formattedDate = dateFormat.format(sunday.getTime());

        workArrangementId = generateValidDocumentId(managerEmail, formattedDate);

        db.collection("Work Arrangement").document(workArrangementId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Work arrangement exists. Loading data...");
                        workSchedule = new WorkSchedule(formattedDate);
                    } else {
                        Log.d(TAG, "No work arrangement found. Creating a new one for Sunday...");
                        workSchedule = new WorkSchedule(formattedDate);
                        workSchedule.saveToFirestore(workArrangementId);
                    }
                    updateShiftsOnUI(); // 🔥 Ensure UI updates immediately after data fetch
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching work arrangement", e);
                    updateShiftsOnUI(); // 🔥 Ensure UI still updates even if Firestore fails
                });
    }


    public static String generateValidDocumentId(String email, String formattedDate) {
        return email.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + formattedDate.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private void updateShiftsOnUI() {
        morningShiftLayout.removeAllViews();
        eveningShiftLayout.removeAllViews();

        if (workSchedule == null || workSchedule.getSchedule() == null) {
            Log.e("updateShiftsOnUI", "WorkSchedule or schedule map is null!");
            return;
        }

        // Get the correct date format for shift retrieval
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M", Locale.getDefault());
        Calendar calendar = (Calendar) currentWeek.clone();

        String[] daysToDisplay = new String[3];
        for (int i = -1; i <= 1; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, i);
            daysToDisplay[i + 1] = dateFormat.format(calendar.getTime()); // Corrected index
            calendar.add(Calendar.DAY_OF_MONTH, -i); // Reset calendar position
        }

        for (String shiftType : new String[]{"morning", "evening"}) {
            LinearLayout shiftRow = shiftType.equals("morning") ? morningShiftLayout : eveningShiftLayout;
            shiftRow.removeAllViews(); // Clear previous shifts

            for (String day : daysToDisplay) {
                List<Map<String, String>> shifts = workSchedule.getSchedule()
                        .getOrDefault(day, new HashMap<>())
                        .getOrDefault(shiftType, new ArrayList<>());

                if (!shifts.isEmpty()) {
                    for (Map<String, String> shift : shifts) {
                        createShiftView(shift, day, shiftType);
                    }
                } else {
                    createEmptyShiftView(shiftRow, shiftType, day);
                }
            }
        }
    }

    private void createEmptyShiftView(LinearLayout parentLayout, String shiftType, String day) {
        LinearLayout emptyShift = new LinearLayout(this);
        emptyShift.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160, 1)); // Make it fit the row
        emptyShift.setGravity(Gravity.CENTER);
        emptyShift.setPadding(8, 8, 8, 8);
        emptyShift.setBackgroundResource(R.drawable.shift_background);
        emptyShift.setOrientation(LinearLayout.VERTICAL);

        ImageButton addButton = new ImageButton(this);
        addButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        addButton.setImageResource(R.drawable.ic_add);
        addButton.setBackgroundResource(android.R.color.transparent);
        addButton.setContentDescription("Add Shift");
        try {
            addButton.setOnClickListener(view -> {
                ShiftDialogFragment shiftDialog = new ShiftDialogFragment(
                        managerEmail,
                        workArrangementId,
                        day,
                        shiftType.toLowerCase(),
                        workSchedule,
                        this::getWorkArrangement // Refresh after adding shift
                );
                shiftDialog.show(getSupportFragmentManager(), "ShiftDialog");
            });
        } catch (Exception e) {
            Log.e("shiftDialog", "Shift Dialog Crashed!");
        }

        emptyShift.addView(addButton);
        parentLayout.addView(emptyShift);
    }


    private void createShiftView(Map<String, String> shift, String day, String shiftType) {
        LinearLayout shiftContainer = new LinearLayout(this);
        shiftContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100));
        shiftContainer.setGravity(Gravity.CENTER);
        shiftContainer.setPadding(8, 8, 8, 8);
        shiftContainer.setBackgroundResource(R.drawable.shift_background);
        shiftContainer.setOrientation(LinearLayout.VERTICAL);

        TextView employeeNameView = new TextView(this);
        employeeNameView.setText(shift.get("name"));
        employeeNameView.setTextSize(16);
        shiftContainer.addView(employeeNameView);

        shiftContainer.setOnClickListener(view -> openShiftDialog(day, shiftType, shift));

        if ("morning".equals(shiftType)) {
            morningShiftLayout.addView(shiftContainer);
        } else {
            eveningShiftLayout.addView(shiftContainer);
        }
    }

    private void openShiftDialog(String day, String shiftType, Map<String, String> shift) {
        ShiftDialogFragment shiftDialog = new ShiftDialogFragment(
                managerEmail,
                workArrangementId,
                day,
                shiftType,
                workSchedule,
                this::getWorkArrangement
        );
        shiftDialog.show(getSupportFragmentManager(), "ShiftDialog");
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.m_my_profile) {
            intent = new Intent(this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            intent = new Intent(this, Login.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
