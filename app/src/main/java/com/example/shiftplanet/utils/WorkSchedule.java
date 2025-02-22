package com.example.shiftplanet.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;
import com.google.firebase.firestore.SetOptions; // ✅ Import this at the top

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
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // If the document exists, merge the existing data with the new data
                        db.collection("Work Arrangement").document(documentId)
                                .set(this.toMap(), SetOptions.merge())
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Schedule merged successfully!"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error merging schedule: " + e.getMessage(), e));
                    } else {
                        // If the document does not exist, create a new one
                        db.collection("Work Arrangement").document(documentId)
                                .set(this.toMap())
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "New schedule created successfully!"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error creating schedule: " + e.getMessage(), e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking if document exists: " + e.getMessage(), e));
    }



    // ✅ Add Employee to Firestore Schedule
    public void addEmployeeToShift(String documentId, String day, String shift, String startTime, String endTime, String email, String name) {
        db.collection("Work Arrangement").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> existingData = documentSnapshot.getData();
                        if (existingData == null) {
                            existingData = new HashMap<>();
                        }

                        // Retrieve the existing schedule from Firestore
                        Map<String, Map<String, List<Map<String, String>>>> currentSchedule =
                                (Map<String, Map<String, List<Map<String, String>>>>) existingData.get("schedule");

                        if (currentSchedule == null) {
                            currentSchedule = new HashMap<>();
                        }

                        // Ensure day and shift keys exist
                        currentSchedule.putIfAbsent(day, new HashMap<>());
                        currentSchedule.get(day).putIfAbsent(shift, new ArrayList<>());

                        List<Map<String, String>> shiftList = currentSchedule.get(day).get(shift);

                        // Check if the employee already has a shift, if so, update it
                        boolean shiftExists = false;
                        for (Map<String, String> shiftEntry : shiftList) {
                            if (shiftEntry.get("email").equals(email)) {
                                shiftExists = true;
                                shiftEntry.put("start_time", startTime);
                                shiftEntry.put("end_time", endTime);
                                break;
                            }
                        }

                        // If employee is not found, add a new shift entry
                        if (!shiftExists) {
                            Map<String, String> newShift = new HashMap<>();
                            newShift.put("email", email);
                            newShift.put("name", name);
                            newShift.put("start_time", startTime);
                            newShift.put("end_time", endTime);
                            shiftList.add(newShift);
                        }

                        // Save the updated schedule back to Firestore
                        db.collection("Work Arrangement").document(documentId)
                                .update("schedule", currentSchedule)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Shift updated successfully!"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating schedule: " + e.getMessage(), e));

                    } else {
                        // If the document does not exist, create a new one
                        Log.d("Firestore", "No existing document found. Creating a new work schedule.");
                        saveToFirestore(documentId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching work arrangement: " + e.getMessage(), e));
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

    public Map<String, Map<String, List<Map<String, String>>>> getSchedule() {
        return schedule;
    }

}
