package com.example.shiftplanet;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class EmployeeRequestPage extends AppCompatActivity {

    private EditText startDateEditText, endDateEditText;
    private AutoCompleteTextView reasonDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_request_page);

        // Initialize views
        startDateEditText = findViewById(R.id.start_date);
        endDateEditText = findViewById(R.id.end_date);
        reasonDropdown = findViewById(R.id.reason_dropdown);

        // Setup dropdown menu
        String[] reasons = {"Vacation", "Sick Leave"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, reasons);
        reasonDropdown.setAdapter(adapter);

        // Setup date pickers
        startDateEditText.setOnClickListener(v -> showDatePicker((date) -> startDateEditText.setText(date)));
        endDateEditText.setOnClickListener(v -> showDatePicker((date) -> endDateEditText.setText(date)));
    }

    private void showDatePicker(OnDateSelectedListener listener) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            listener.onDateSelected(date);
        }, year, month, day).show();
    }

    interface OnDateSelectedListener {
        void onDateSelected(String date);
    }
}
