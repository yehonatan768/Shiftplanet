package com.example.shiftplanet;

import com.example.shiftplanet.utils.WorkSchedule;
import com.example.shiftplanet.dialogs.ShiftDialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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

        Calendar sunday = (Calendar) currentWeek.clone();
        sunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String formattedDate = format.format(sunday.getTime());

        calendarTitle.setText("Work Arrangement - " + formattedDate);

        updateDateDisplay();
        getWorkArrangement();
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

        Calendar sunday = (Calendar) currentWeek.clone();
        sunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String formattedDate = dateFormat.format(sunday.getTime());

        workArrangementId = generateValidDocumentId(managerEmail, formattedDate);

        db.collection("Work Arrangement").document(workArrangementId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("Firestore", "Work arrangement exists. Loading data...");

                        Map<String, Object> data = documentSnapshot.getData();
                        Log.d("Firestore", "Raw data from Firestore: " + data);
                        workSchedule = new WorkSchedule(formattedDate);

                        if (data != null && data.containsKey("schedule")) {
                            Map<String, Object> scheduleData = (Map<String, Object>) data.get("schedule");
                            Log.d("Firestore", "Schedule data: " + scheduleData);

                            for (String day : workSchedule.getSchedule().keySet()) {
                                if (scheduleData.containsKey(day)) {
                                    Map<String, Object> shiftMap = (Map<String, Object>) scheduleData.get(day);
                                    Log.d("Firestore", "Data for day " + day + ": " + shiftMap);

                                    for (String shiftType : workSchedule.getSchedule().get(day).keySet()) {
                                        if (shiftMap.containsKey(shiftType)) {
                                            List<Map<String, String>> shiftList = (List<Map<String, String>>) shiftMap.get(shiftType);
                                            workSchedule.getSchedule().get(day).put(shiftType, shiftList);
                                            Log.d("Firestore", "Data for day " + day + ", shift " + shiftType + ": " + shiftList);
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        Log.d("Firestore", "No work arrangement found. Creating a new one for Sunday...");
                        workSchedule = new WorkSchedule(formattedDate);
                        workSchedule.saveToFirestore(workArrangementId);
                    }

                    updateShiftsOnUI();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching work arrangement", e);
                    updateShiftsOnUI();
                });

    }

    public static String generateValidDocumentId(String email, String formattedDate) {
        return email.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + formattedDate.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Updates the shift views on the UI for both morning and evening shifts.
     * <p>
     * This method clears the current shift layouts, then retrieves the shift data
     * from the workSchedule object. It formats the dates for the current day,
     * previous day, and next day, then iterates over each shift type ("morning" and "evening")
     * to populate the layouts. For each day and shift type, it either creates a shift view
     * if there is scheduled shift data or an empty shift view with an add button if there isn't.
     * </p>
     */

    private void updateShiftsOnUI() {

        morningShiftLayout.removeAllViews();
        eveningShiftLayout.removeAllViews();

        if (workSchedule == null || workSchedule.getSchedule() == null) {
            Log.e("updateShiftsOnUI", "WorkSchedule or schedule map is null!");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        Calendar calendar = (Calendar) currentWeek.clone();

        String[] daysToDisplay = new String[3];
        for (int i = -1; i <= 1; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, i);
            daysToDisplay[i + 1] = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
        }

        for (String shiftType : new String[]{"morning", "evening"}) {
            LinearLayout shiftContainer = shiftType.equals("morning") ? morningShiftLayout : eveningShiftLayout;
            shiftContainer.removeAllViews();

            List<Map<String, String>>[] shiftLists = new List[3];
            for (int i = 0; i < 3; i++) {
                String day = daysToDisplay[i];
                shiftLists[i] = workSchedule.getSchedule()
                        .getOrDefault(day, new HashMap<>())
                        .getOrDefault(shiftType, new ArrayList<>());
            }

            int maxSize = 0;
            for (int i = 0; i < 3; i++) {
                if (shiftLists[i].size() > maxSize) {
                    maxSize = shiftLists[i].size();
                }
            }

            int numRows = maxSize + 1;

            for (int row = 0; row < numRows; row++) {
                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(60)
                );
                if (row > 0) {
                    rowParams.topMargin = dpToPx(8);
                }
                rowLayout.setLayoutParams(rowParams);

                for (int col = 0; col < 3; col++) {
                    View cellView;
                    List<Map<String, String>> currentList = shiftLists[col];
                    String day = daysToDisplay[col];

                    if (row < currentList.size()) {
                        Map<String, String> shift = currentList.get(row);
                        cellView = buildShiftItemView(shift, day, shiftType);
                    } else {
                        cellView = buildEmptyShiftItemView(day, shiftType);
                        Log.d("Firestore", "No shift at row " + row + " for " + day + " (" + shiftType + ")");
                    }

                    LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                    );
                    cellView.setLayoutParams(cellParams);
                    rowLayout.addView(cellView);
                }

                shiftContainer.addView(rowLayout);
            }
        }
    }


    private View buildShiftItemView(Map<String, String> shift, String day, String shiftType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.item_shift_employee, null);

        TextView tvEmployeeName = view.findViewById(R.id.tv_employee_name);
        TextView btnStartTime = view.findViewById(R.id.btn_start_time);
        TextView btnEndTime = view.findViewById(R.id.btn_end_time);

        tvEmployeeName.setText(shift.get("name"));
        btnStartTime.setText(shift.get("start_time"));
        btnEndTime.setText(shift.get("end_time"));

        view.setOnClickListener(v -> openShiftDialog(day, shiftType));

        return view;
    }


    private View buildEmptyShiftItemView(String day, String shiftType) {
        LinearLayout emptyShift = new LinearLayout(this);
        emptyShift.setOrientation(LinearLayout.VERTICAL);
        emptyShift.setGravity(Gravity.CENTER);
        emptyShift.setPadding(8, 8, 8, 8);
        emptyShift.setBackgroundResource(R.drawable.shift_background);

        ImageButton addButton = new ImageButton(this);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(100, 100);
        addButton.setLayoutParams(buttonParams);
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
                        this::getWorkArrangement
                );
                shiftDialog.show(getSupportFragmentManager(), "ShiftDialog");
            });
        } catch (Exception e) {
            Log.e("shiftDialog", "Shift Dialog Crashed!", e);
        }
        emptyShift.addView(addButton);
        return emptyShift;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }


    private void openShiftDialog(String day, String shiftType) {
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
        if (item.getItemId() == R.id.m_home_page) {
            Toast.makeText(ManagerWorkArrangement.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerWorkArrangement.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerWorkArrangement.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerWorkArrangement.this, ManagerProfile.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerWorkArrangement.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerWorkArrangement.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerWorkArrangement.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerWorkArrangement.this, ManagerWorkArrangement.class);
        }  else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerWorkArrangement.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerWorkArrangement.this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerWorkArrangement.this, "Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerWorkArrangement.this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerWorkArrangement.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerWorkArrangement.this, Login.class);
        }

        if (intent != null) {
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

