package com.example.shiftplanet.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class WorkSchedule {
    private String week;
    private Map<String, Map<String, List<Map<String, String>>>> schedule;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance

    public WorkSchedule(String startDate) {
        this.week = startDate;
        this.schedule = new LinkedHashMap<>();

        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] shifts = {"morning", "evening"};

        for (String day : days) {
            Map<String, List<Map<String, String>>> shiftMap = new LinkedHashMap<>();
            for (String shift : shifts) {
                shiftMap.put(shift, new ArrayList<>()); // Empty list for shifts
            }
            schedule.put(day, shiftMap);
        }
    }

    // Convert WorkSchedule to a Firestore-compatible Map
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("week", this.week);
        result.put("schedule", this.schedule);
        return result;
    }

    // ✅ Save WorkSchedule to Firestore
    public void saveToFirestore(String documentId) {
        db.collection("Work Arrangement").document(documentId)
                .set(this.toMap())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "JSON saved successfully!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving JSON: " + e.getMessage(), e));
    }

    // ✅ Add Employee to Firestore Schedule
    public void addEmployeeToShift(String documentId, String day, String shift, String startTime, String endTime, String email, String name) {
        if (!schedule.containsKey(day) || !schedule.get(day).containsKey(shift)) {
            Log.e("Firestore", "Error: Invalid day or shift type.");
            return;
        }

        // Create new shift entry
        Map<String, String> shiftEntry = new LinkedHashMap<>();
        shiftEntry.put("email", email);
        shiftEntry.put("name", name);
        shiftEntry.put("start_time", startTime);
        shiftEntry.put("end_time", endTime);

        // Add shift to schedule
        schedule.get(day).get(shift).add(shiftEntry);

        // Update Firestore
        saveToFirestore(documentId);
    }

    // ✅ Remove Employee from Firestore Schedule
    public void removeEmployeeFromShift(String documentId, String day, String shift, String email) {
        if (!schedule.containsKey(day) || !schedule.get(day).containsKey(shift)) {
            Log.e("Firestore", "Error: Invalid day or shift type.");
            return;
        }

        // Find and remove the shift for the specified email
        boolean removed = schedule.get(day).get(shift).removeIf(entry -> email.equals(entry.get("email")));

        if (!removed) {
            Log.e("Firestore", "Error: Employee not found in the shift.");
            return;
        }

        // Update Firestore
        saveToFirestore(documentId);
    }
}
