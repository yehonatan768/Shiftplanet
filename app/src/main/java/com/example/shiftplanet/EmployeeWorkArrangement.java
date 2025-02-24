package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.shiftplanet.dialogs.ShiftDialogFragment;
import com.example.shiftplanet.utils.WorkSchedule;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeeWorkArrangement extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private String managerEmail, employeeEmail;
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
        setContentView(R.layout.employee_work_arrangement_page);

        db = FirebaseFirestore.getInstance();

        employeeEmail = getIntent().getStringExtra("LOGIN_EMAIL");
        if (employeeEmail == null) {
            Log.e("Firestore", "Employee email is not set");
        } else {
            getManagerEmail();
            Log.d("Firestore", "done");
        }


        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.employee_work_arrangement);
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
        if (managerEmail != null) {
            getWorkArrangement();
        }

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

    private void getManagerEmail() {
        try {
            Log.d("Firestore", "getManagerEmail() called, searching for email: " + employeeEmail);

            if (employeeEmail == null || employeeEmail.isEmpty()) {
                Log.e("Firestore", "Error: Employee email is null or empty!");
                return;
            }

            if (db == null) {
                Log.e("Firestore", "Firestore instance (db) is NULL!");
                return;
            }

            db.collection("users")
                    .whereEqualTo("email", employeeEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            Log.d("Firestore", "Query executed successfully, checking results...");

                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                Log.d("Firestore", "Document found: " + document.getId());

                                if (document.contains("managerEmail")) {
                                    managerEmail = document.getString("managerEmail");
                                    Log.d("Firestore", "✅ Manager email found: " + managerEmail);


                                    Log.d("Firestore", "Employee email is " + employeeEmail + ". Manager email is " + managerEmail);

                                    getWorkArrangement();
                                } else {
                                    Log.e("Firestore", "❌ Manager email field does not exist in document!");
                                }
                            } else {
                                Log.e("Firestore", "❌ No user found with email: " + employeeEmail);
                            }
                        } catch (Exception e) {
                            Log.e("Firestore", "❌ Error processing Firestore response", e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "❌ Firestore query failed: " + e.getMessage(), e);
                        try {
                            Toast.makeText(getApplicationContext(), "Error fetching manager email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (Exception toastException) {
                            Log.e("Firestore", "❌ Toast display failed!", toastException);
                        }
                    });

        } catch (Exception e) {
            Log.e("Firestore", "❌ getManagerEmail() failed!", e);
        }
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

            int numRows = maxSize;

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
                        Map<String, String> emptyShift = new HashMap<>();
                        emptyShift.put("email", "");
                        emptyShift.put("end_time", "");
                        emptyShift.put("name", "");
                        emptyShift.put("start_time", "");
                        cellView = buildShiftItemView(emptyShift, day, shiftType);
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

        return view;
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        if (item.getItemId() == R.id.e_home_page) {
            Toast.makeText(EmployeeWorkArrangement.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, EmployeeHomePage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_my_profile) {
            Toast.makeText(EmployeeWorkArrangement.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, ManagerProfile.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_work_arrangement) {
            Toast.makeText(EmployeeWorkArrangement.this, "Work arrangement clicked", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.constraints) {
            Toast.makeText(EmployeeWorkArrangement.this, "Constraints clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, EmployeeSubmitConstraintsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.day_off) {
            Toast.makeText(EmployeeWorkArrangement.this, "Day off clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, EmployeeRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.shift_change) {
            Toast.makeText(EmployeeWorkArrangement.this, "Shift change clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, EmployeeShiftChangeRequest.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.requests_status) {
            Toast.makeText(EmployeeWorkArrangement.this, "Requests status clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, EmployeeRequestStatus.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.notification) {
            Toast.makeText(EmployeeWorkArrangement.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        } else if (item.getItemId() == R.id.e_log_out) {
            Toast.makeText(EmployeeWorkArrangement.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(EmployeeWorkArrangement.this, Login.class);
            intent.putExtra("LOGIN_EMAIL", employeeEmail);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        if (intent != null) {
            startActivity(intent);
            finish();
        }
        return true;
    }
}
