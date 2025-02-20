package com.example.shiftplanet.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.shiftplanet.R;

import java.util.Locale;

public class ShiftDialogFragment extends DialogFragment {

    private String employeeName;
    private String startTime;
    private String endTime;

    private TextView btnStartTime, btnEndTime;
    private ShiftDialogListener listener;

    // Interface for sending data back to the MainActivity
    public interface ShiftDialogListener {
        void onShiftTimeSelected(String updatedStartTime, String updatedEndTime);
    }

    public ShiftDialogFragment(String employeeName, String startTime, String endTime, ShiftDialogListener listener) {
        this.employeeName = employeeName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate layout
        View view = LayoutInflater.from(getContext()).inflate(R.layout.shift_dialog, null);
        dialog.setContentView(view);

        // Initialize UI elements
        TextView tvEmployeeName = view.findViewById(R.id.tv_employee_name);
        btnStartTime = view.findViewById(R.id.btn_start_time);
        btnEndTime = view.findViewById(R.id.btn_end_time);

        // Set initial text values
        tvEmployeeName.setText(employeeName);
        btnStartTime.setText(startTime);
        btnEndTime.setText(endTime);

        // Handle start time click
        btnStartTime.setOnClickListener(v -> showTimePickerDialog(true));

        // Handle end time click
        btnEndTime.setOnClickListener(v -> showTimePickerDialog(false));

        return dialog;
    }

    private void showTimePickerDialog(boolean isStartTime) {
        // Get current hour and minute
        int hour = 12, minute = 0;
        if (isStartTime && startTime != null && !startTime.isEmpty()) {
            String[] parts = startTime.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } else if (!isStartTime && endTime != null && !endTime.isEmpty()) {
            String[] parts = endTime.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        }

        // Show 24-hour format TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // Default theme
                (view, selectedHour, selectedMinute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    if (isStartTime) {
                        startTime = formattedTime;
                        btnStartTime.setText(formattedTime);
                    } else {
                        endTime = formattedTime;
                        btnEndTime.setText(formattedTime);
                    }

                    // Send updated time back to MainActivity
                    if (listener != null) {
                        listener.onShiftTimeSelected(startTime, endTime);
                    }
                },
                hour, minute, true
        );


        timePickerDialog.show(); // The "OK" button will be included automatically
    }
}
