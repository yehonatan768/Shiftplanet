package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EmployeeShiftChangeDialog extends AppCompatActivity {

    private String notificationId; // The ID of the notification
    protected DocumentSnapshot requestDocument;
    private TextView employeeName, datesAndHours, detailsText;
    private Button approveButton, denyButton;
    private ImageView btnBack;
    private String currentEmployeeName;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_shiftchange_dialog);

        btnBack = findViewById(R.id.btnBackDialog);
        employeeName = findViewById(R.id.employee_name);
        datesAndHours = findViewById(R.id.datesAndHours);
        detailsText = findViewById(R.id.details_text);
        approveButton = findViewById(R.id.approve_button);
        denyButton = findViewById(R.id.deny_button);


        notificationId = getIntent().getStringExtra("notificationId");

        if (notificationId != null) {
            fetchRequestDetails(notificationId);
        } else {
            Toast.makeText(this, "Error: No notification ID found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeShiftChangeDialog.this, EmployeeNotificationsPage.class);
            intent.putExtra("LOGIN_EMAIL", current.getEmail());
            startActivity(intent);
            finish();
        });

        denyButton.setOnClickListener(v -> finish());
        approveButton.setOnClickListener(v -> {
            handleApproveAction();
            finish();
        });
    }

    private void fetchRequestDetails(String notificationId) {

        db.collection("ShiftChangeRequests").document(notificationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //שליפת הנתונים מהמסמך
                        String employee = documentSnapshot.getString("employeeName") + " wants to switch shifts!";
                        String date = documentSnapshot.getString("Date") + ", " + documentSnapshot.getString("Hours") ;
                        String details = documentSnapshot.getString("details");

                        // הצגת הנתונים ב-UI
                        employeeName.setText(employee);
                        datesAndHours.setText(date);
                        detailsText.setText(details);

                    } else {
                        Toast.makeText(this, "Request not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }


    void handleApproveAction() {
        if (notificationId == null) {
            Toast.makeText(this, "Error: Missing request ID!", Toast.LENGTH_SHORT).show();
            return;
        }


        // שליפת הבקשה הנוכחית
        db.collection("ShiftChangeRequests").document(notificationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // קבלת כל המידע מהבקשה הנוכחית
                        String employeeName = documentSnapshot.getString("employeeName");
                        String date = documentSnapshot.getString("Date");
                        String hours = documentSnapshot.getString("Hours");
                        String managerEmail = documentSnapshot.getString("managerEmail");
                        String details = documentSnapshot.getString("details");
                        long requestNumber = documentSnapshot.getLong("requestNumber");
                        fetchEmployeeName();
                        // עדכון הבקשה הנוכחית
                        db.collection("ShiftChangeRequests").document(notificationId)
                                .update("approvedByEmployee", true) // עדכון הסטטוס של העובד
                                .addOnSuccessListener(aVoid -> {

                                    Map<String, Object> request = new HashMap<>();
                                    request.put("type", "shift change");
                                    request.put("requestNumber", requestNumber);
                                    request.put("employeeName", employeeName);
                                    request.put("date", date);
                                    request.put("hours", hours);
                                    request.put("details", details);
                                    request.put("managerEmail", managerEmail);
                                    request.put("timestamp", FieldValue.serverTimestamp());
                                    request.put("switch employee", currentEmployeeName);
                                    request.put("status", "pending");


                                    db.collection("Requests")
                                            .add(request)
                                            .addOnSuccessListener(documentReference -> {
                                                Toast.makeText(EmployeeShiftChangeDialog.this, "shift change submitted for manager approval", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(EmployeeShiftChangeDialog.this, "Error submitting shift change", Toast.LENGTH_SHORT).show();
                                            });

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(EmployeeShiftChangeDialog.this, "Error updating request: " + e.getMessage() + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // אם הבקשה לא קיימת במסד הנתונים
                        Toast.makeText(EmployeeShiftChangeDialog.this, "Request not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // במקרה של כישלון בשאילתת ה-Firestore
                    Toast.makeText(EmployeeShiftChangeDialog.this, "Error fetching request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

                    private void fetchEmployeeName () {
                        db.collection("users").document(current.getUid()).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        currentEmployeeName = documentSnapshot.getString("fullname"); // לוודא שזה השדה הנכון
                                        Log.d(TAG, "Employee name retrieved: " + employeeName);
                                    } else {
                                        Log.e(TAG, "Employee document does not exist");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error fetching employee name", e);
                                });
                    }
    }



