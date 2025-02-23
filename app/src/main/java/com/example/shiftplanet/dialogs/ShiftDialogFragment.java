package com.example.shiftplanet.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.shiftplanet.R;
import com.example.shiftplanet.utils.WorkSchedule;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShiftDialogFragment extends DialogFragment {

    private List<String> employees;
    private List<String> employeesEmail;
    private String selectedEmployee;
    private String selectedEmployeeEmail;
    private String startTime;
    private String endTime;
    private String shiftType;
    private String managerEmail;
    private String workArrangementId;
    private String shiftDay;
    private WorkSchedule workSchedule;

    private AutoCompleteTextView employeeDropdown;
    private TextView btnStartTime, btnEndTime, btnUpdateShift, btnDeleteShift;
    private ShiftDialogListener listener;

    public interface ShiftDialogListener {
        void onShiftUpdated();
    }

    public ShiftDialogFragment(String managerEmail, String workArrangementId, String shiftDay, String shiftType,
                               WorkSchedule workSchedule, ShiftDialogListener listener) {
        this.managerEmail = managerEmail;
        this.workArrangementId = workArrangementId;
        this.shiftDay = shiftDay;
        this.shiftType = shiftType;
        this.workSchedule = workSchedule;
        this.listener = listener;
        this.employees = new ArrayList<>();
        this.employeesEmail = new ArrayList<>();

        try {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), managerEmail, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("ShiftDialog", "Fragment not attached, cannot show Toast.");
            }
        } catch (Exception e) {
            Log.e("ShiftDialog", "Error displaying Toast", e);
        }

        // ✅ Set default shift times
        if ("morning".equals(shiftType)) {
            this.startTime = "07:30";
            this.endTime = "16:00";
        } else {
            this.startTime = "16:00";
            this.endTime = "23:30";
        }
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getContext() != null) {
            Toast.makeText(requireContext(), managerEmail, Toast.LENGTH_SHORT).show();
        }

        fetchEmployees();

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.shift_dialog, null);
        dialog.setContentView(view);

        // ✅ Fixed references to match XML updates
        employeeDropdown = view.findViewById(R.id.autoCompleteEmployeeName);
        btnStartTime = view.findViewById(R.id.btn_start_time);
        btnEndTime = view.findViewById(R.id.btn_end_time);
        btnUpdateShift = view.findViewById(R.id.btn_update_shift);
        btnDeleteShift = view.findViewById(R.id.btn_delete_shift);

        btnStartTime.setText(startTime);
        btnEndTime.setText(endTime);

        btnStartTime.setOnClickListener(v -> showTimePickerDialog(true));
        btnEndTime.setOnClickListener(v -> showTimePickerDialog(false));

        btnUpdateShift.setOnClickListener(v -> addOrUpdateShift());
        btnDeleteShift.setOnClickListener(v -> deleteShift());

        return dialog;
    }

    private void addOrUpdateShift() {
        if (selectedEmployee == null || selectedEmployeeEmail == null) {
            Toast.makeText(getContext(), "Select an employee", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean shiftExists = false;
        List<Map<String, String>> existingShifts = workSchedule.getSchedule()
                .getOrDefault(shiftDay, new HashMap<>())
                .getOrDefault(shiftType, new ArrayList<>());

        // Check if there's already a shift with this email
        for (Map<String, String> shift : existingShifts) {
            // FIXED: Use equals(...) to detect the same email
            if (shift.get("email").equals(selectedEmployeeEmail)) {
                shiftExists = true;
                // Update existing shift in memory
                shift.put("email", selectedEmployeeEmail);
                shift.put("name", selectedEmployee);
                shift.put("start_time", startTime);
                shift.put("end_time", endTime);
                break;
            }
        }

        if (!shiftExists) {
            // If we never found the shift, we add a new one
            workSchedule.addEmployeeToShift(
                    workArrangementId,
                    shiftDay,
                    shiftType,
                    startTime,
                    endTime,
                    selectedEmployeeEmail,
                    selectedEmployee
            );
            Toast.makeText(getContext(), "Shift added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // We updated an existing shift in memory, so just save the entire schedule
            workSchedule.saveToFirestore(workArrangementId);
            Toast.makeText(getContext(), "Shift updated successfully!", Toast.LENGTH_SHORT).show();
        }

        if (listener != null) listener.onShiftUpdated();
        dismiss();
    }

    private void deleteShift() {
        if (selectedEmployeeEmail == null) {
            Toast.makeText(getContext(), "No shift selected for deletion", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Check if the shift exists before trying to delete
        List<Map<String, String>> existingShifts = workSchedule.getSchedule()
                .getOrDefault(shiftDay, new HashMap<>())
                .getOrDefault(shiftType, new ArrayList<>());

        boolean shiftFound = false;
        for (int i = 0; i < existingShifts.size(); i++) {
            if (existingShifts.get(i).get("email").equals(selectedEmployeeEmail)) {
                existingShifts.remove(i);
                shiftFound = true;
                break;
            }
        }

        if (shiftFound) {
            workSchedule.saveToFirestore(workArrangementId); // ✅ Update Firestore
            Toast.makeText(getContext(), "Shift deleted successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Shift not found!", Toast.LENGTH_SHORT).show();
        }

        if (listener != null) listener.onShiftUpdated();
        dismiss();
    }



    /**
     * ✅ Shows a TimePickerDialog with an OK button.
     */
    private void showTimePickerDialog(boolean isStartTime) {
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

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, selectedHour, selectedMinute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    if (isStartTime) {
                        startTime = formattedTime;
                        btnStartTime.setText(formattedTime);
                    } else {
                        endTime = formattedTime;
                        btnEndTime.setText(formattedTime);
                    }

                    // Notify listener
                    if (listener != null) {
                        listener.onShiftUpdated();
                    }
                },
                hour, minute, true
        );

        timePickerDialog.show(); // ✅ OK button will be included automatically
    }

    /**
     * ✅ Fetch employees under the same manager.
     */
    private void fetchEmployees() {
        if (managerEmail == null || managerEmail.isEmpty()) {
            Log.e("Firestore", "fetchEmployees() failed: managerEmail is null or empty.");
            return;
        }

        // ✅ Normalize email to avoid Firestore mismatches
        managerEmail = managerEmail.trim().toLowerCase();
        Log.d("Firestore", "Fetching employees for managerEmail: " + managerEmail);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        employees.clear();

        try {
            // ✅ Step 1: Fetch Manager's Name
            db.collection("users")
                    .whereEqualTo("email", managerEmail)
                    .get()
                    .addOnSuccessListener(managerQuerySnapshot -> {
                        // Log.d("Firestore", "Manager query result size: " + managerQuerySnapshot.size());

                        if (!managerQuerySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot doc : managerQuerySnapshot) {
                                // Log.d("Firestore", "Document Data: " + doc.getData().toString()); // Print entire document

                                String managerName = doc.getString("fullname");

                                if (managerName != null) {

                                    employees.add(managerName);
                                    employeesEmail.add(managerEmail);

                                    // Log.d("Firestore", "✅ Manager found: " + managerName);
                                } else {
                                    Log.e("Firestore", "❌ 'name' field is missing in document: " + doc.getId());
                                }
                            }
                        }

                        // ✅ Step 2: Fetch Employees
                        db.collection("users")
                                .whereEqualTo("managerEmail", managerEmail)
                                .get()
                                .addOnSuccessListener(employeeQuerySnapshot -> {
                                    // Log.d("Firestore", "Employee query result size: " + employeeQuerySnapshot.size());

                                    if (!employeeQuerySnapshot.isEmpty()) {
                                        for (QueryDocumentSnapshot document : employeeQuerySnapshot) {
                                            // Log.d("Firestore", "Document Data: " + document.getData().toString()); // Print document

                                            String name = document.getString("fullname");
                                            String employeeEmail = document.getString("email");
                                            if (name != null) {
                                                employees.add(name);
                                                employeesEmail.add(employeeEmail);
                                                // Log.d("Firestore", "✅ Employee found: " + name);
                                            } else {
                                                // Log.e("Firestore", "❌ 'name' field missing in employee document: " + document.getId());
                                            }
                                        }
                                    }

                                    // ✅ Update UI after fetching data
                                    updateAutoCompleteTextView();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "❌ Error fetching employees: " + e.getMessage(), e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "❌ Error fetching manager: " + e.getMessage(), e);
                    });

        } catch (Exception e) {
            Log.e("Firestore", "❌ Firestore query crashed!", e);
        }
        Log.d("Firestore", "✅ Fetch employees successfully!");
    }



    /**
     * ✅ Updates AutoCompleteTextView with the list of employees.
     */
    private void updateAutoCompleteTextView() {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, employees);
            employeeDropdown.setAdapter(adapter);
            employeeDropdown.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    employeeDropdown.showDropDown();
                }
            });

            employeeDropdown.setOnClickListener(v -> employeeDropdown.showDropDown());

            employeeDropdown.setOnItemClickListener((parent, view, position, id) -> {
                selectedEmployee = employees.get(position);
                selectedEmployeeEmail = employeesEmail.get(position);
            });

            Log.d("UI", "AutoCompleteTextView updated with " + employees.size() + " employees");
        });
    }
}
