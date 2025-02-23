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
                        Log.d("Firestore", "Work arrangement exists. Loading data...");

                        // ✅ Correctly load Firestore data into WorkSchedule
                        Map<String, Object> data = documentSnapshot.getData();
                        // Log the raw data fetched from Firestore
                        Log.d("Firestore", "Raw data from Firestore: " + data);
                        workSchedule = new WorkSchedule(formattedDate); // Initialize

                        // Retrieve the schedule map from the raw data using the "schedule" key
                        if (data != null && data.containsKey("schedule")) {
                            Map<String, Object> scheduleData = (Map<String, Object>) data.get("schedule");
                            Log.d("Firestore", "Schedule data: " + scheduleData);

                            // Iterate through each day in the WorkSchedule's schedule map
                            for (String day : workSchedule.getSchedule().keySet()) {
                                if (scheduleData.containsKey(day)) {
                                    Map<String, Object> shiftMap = (Map<String, Object>) scheduleData.get(day);
                                    // Log the data for the specific day
                                    Log.d("Firestore", "Data for day " + day + ": " + shiftMap);

                                    // Iterate through each shift type ("morning", "evening") for the day
                                    for (String shiftType : workSchedule.getSchedule().get(day).keySet()) {
                                        if (shiftMap.containsKey(shiftType)) {
                                            List<Map<String, String>> shiftList = (List<Map<String, String>>) shiftMap.get(shiftType);
                                            workSchedule.getSchedule().get(day).put(shiftType, shiftList);
                                            // Log the data for the specific shift type on that day
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
        // Clear previous shift views from the morning and evening layouts.
        morningShiftLayout.removeAllViews();
        eveningShiftLayout.removeAllViews();

        // Check if workSchedule or its schedule map is null.
        if (workSchedule == null || workSchedule.getSchedule() == null) {
            Log.e("updateShiftsOnUI", "WorkSchedule or schedule map is null!");
            return;
        }

        // Create a date formatter to get the day of the week in full text (e.g., "Monday").
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        // Clone the currentWeek calendar to avoid modifying the original calendar.
        Calendar calendar = (Calendar) currentWeek.clone();

        // Prepare an array to hold the day names for display (previous day, current day, and next day).
        String[] daysToDisplay = new String[3];
        for (int i = -1; i <= 1; i++) {
            // Adjust the calendar to the correct day.
            calendar.add(Calendar.DAY_OF_MONTH, i);
            // Format the date and store it in the array using a corrected index.
            daysToDisplay[i + 1] = dateFormat.format(calendar.getTime());
            // Reset the calendar position by reversing the previous day adjustment.
            calendar.add(Calendar.DAY_OF_MONTH, -i);
        }

        // Iterate over the two shift types: "morning" and "evening".
        for (String shiftType : new String[]{"morning", "evening"}) {
            // Determine the correct layout for the current shift type.
            LinearLayout shiftRow = shiftType.equals("morning") ? morningShiftLayout : eveningShiftLayout;
            // Clear the layout of any existing views.
            shiftRow.removeAllViews();

            // Loop through each day to display shifts.
            for (String day : daysToDisplay) {
                // Retrieve the list of shifts for the given day and shift type from the schedule.
                List<Map<String, String>> shifts = workSchedule.getSchedule()
                        .getOrDefault(day, new HashMap<>())
                        .getOrDefault(shiftType, new ArrayList<>());

                // If there are shifts scheduled, create a view for each shift.
                if (!shifts.isEmpty()) {
                    for (Map<String, String> shift : shifts) {
                        createShiftView(shift, day, shiftType);
                    }
                } else {
                    // Otherwise, create an empty shift view with an add button.
                    createEmptyShiftView(shiftRow, shiftType, day);
                    Log.e("Firestore", "No shifts in " + day + " " + shiftType);
                }
            }
        }
    }

    /**
     * Creates an empty shift view with an add button for a specific day and shift type.
     * <p>
     * This method constructs a LinearLayout representing an empty shift slot. It sets the layout parameters,
     * gravity, padding, and background. An ImageButton is added to the layout, which, when clicked,
     * opens a dialog to add a new shift.
     * </p>
     *
     * @param parentLayout The layout where the empty shift view will be added.
     * @param shiftType    The type of shift ("morning" or "evening").
     * @param day          The day for which the shift view is being created.
     */
    private void createEmptyShiftView(LinearLayout parentLayout, String shiftType, String day) {
        // Create a new LinearLayout to represent the empty shift slot.
        LinearLayout emptyShift = new LinearLayout(this);
        emptyShift.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160, 1)); // Make it fit the row.
        emptyShift.setGravity(Gravity.CENTER);
        emptyShift.setPadding(8, 8, 8, 8);
        emptyShift.setBackgroundResource(R.drawable.shift_background);
        emptyShift.setOrientation(LinearLayout.VERTICAL);

        // Create an ImageButton that will allow the user to add a new shift.
        ImageButton addButton = new ImageButton(this);
        addButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        addButton.setImageResource(R.drawable.ic_add);
        addButton.setBackgroundResource(android.R.color.transparent);
        addButton.setContentDescription("Add Shift");
        try {
            // Set an OnClickListener on the add button to open the ShiftDialogFragment.
            addButton.setOnClickListener(view -> {
                ShiftDialogFragment shiftDialog = new ShiftDialogFragment(
                        managerEmail,
                        workArrangementId,
                        day,
                        shiftType.toLowerCase(),
                        workSchedule,
                        this::getWorkArrangement // Refresh after adding shift.
                );
                shiftDialog.show(getSupportFragmentManager(), "ShiftDialog");
            });
        } catch (Exception e) {
            // Log an error if there is an exception when setting up the shift dialog.
            Log.e("shiftDialog", "Shift Dialog Crashed!");
        }

        // Add the add button to the empty shift layout and then add this layout to the parent layout.
        emptyShift.addView(addButton);
        parentLayout.addView(emptyShift);
    }

    /**
     * Creates a shift view to display a scheduled shift's details.
     * <p>
     * This method creates a LinearLayout to serve as a container for shift details.
     * It adds a TextView displaying the employee's name (retrieved from the shift map) and sets up an OnClickListener
     * that allows the user to edit the shift details by opening a dialog.
     * </p>
     *
     * @param shift     A map containing the shift details (for example, an employee's name).
     * @param day       The day for which the shift is scheduled.
     * @param shiftType The type of shift ("morning" or "evening").
     */
    private void createShiftView(Map<String, String> shift, String day, String shiftType) {
        // Create a container layout for the shift view.
        LinearLayout shiftContainer = new LinearLayout(this);
        shiftContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100));
        shiftContainer.setGravity(Gravity.CENTER);
        shiftContainer.setPadding(8, 8, 8, 8);
        shiftContainer.setBackgroundResource(R.drawable.shift_background);
        shiftContainer.setOrientation(LinearLayout.VERTICAL);

        // Create a TextView to display the employee's name from the shift map.
        TextView employeeNameView = new TextView(this);
        employeeNameView.setText(shift.get("name"));
        employeeNameView.setTextSize(16);
        // Add the TextView to the shift container.
        shiftContainer.addView(employeeNameView);

        // Set an OnClickListener on the shift container to open a dialog for editing the shift when clicked.
        shiftContainer.setOnClickListener(view -> openShiftDialog(day, shiftType));

        // Depending on the shift type, add the shift container to the appropriate layout.
        if ("morning".equals(shiftType)) {
            morningShiftLayout.addView(shiftContainer);
        } else {
            eveningShiftLayout.addView(shiftContainer);
        }
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
        getWorkArrangement();
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
