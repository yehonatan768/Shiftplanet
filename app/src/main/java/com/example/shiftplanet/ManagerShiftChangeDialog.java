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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ManagerShiftChangeDialog extends AppCompatActivity {

    private ImageView btnBack;
    private TextView requestDetailsContent;
    private String notificationId; // The ID of the notification

    private Button approveButton;
    private Button denyButton;
    private TextView names, datesAndHours, detailsText;

    private Button downloadDocument;

    private int requestNumber;

    protected String managerEmail;
    private String employeeEmail;
    protected FirebaseFirestore db;
    protected DocumentSnapshot requestDocument;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_shiftchange_dialog);

        managerEmail = Objects.requireNonNull(getIntent().getStringExtra("managerEmail")).trim();
        employeeEmail = Objects.requireNonNull(getIntent().getStringExtra("employeeEmail")).trim();
        requestNumber = getIntent().getIntExtra("requestNumber", -1);

        if (requestNumber != -1) {
            Log.d(TAG, "Request Number: " + requestNumber);
        } else {
            Toast.makeText(this, "Invalid request number: " + requestNumber, Toast.LENGTH_SHORT).show();
            return;
        }

        btnBack = findViewById(R.id.btnBackDialog);
        names = findViewById(R.id.employees_names);
        datesAndHours = findViewById(R.id.datesAndHours);
        detailsText = findViewById(R.id.details_text);
        approveButton = findViewById(R.id.approve_button);
        denyButton = findViewById(R.id.deny_button);




        db = FirebaseFirestore.getInstance();

        fetchRequestDocument(requestNumber, managerEmail, employeeEmail);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerShiftChangeDialog.this, ManagerRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            startActivity(intent);
            finish();
        });

        //approveButton.setOnClickListener(v -> handleApproveAction());
        //denyButton.setOnClickListener(v -> handleDenyAction());
    }

    private void fetchRequestDocument(int requestNumber, String managerEmail, String employeeEmail) {

        db.collection("Requests")
                .whereEqualTo("requestNumber", requestNumber)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                        String employeesNames = requestDocument.getString("employeeName") + " wants to switch his shift with " + requestDocument.getString("switchName");
                        String datesHours = requestDocument.getString("date") + ", " + requestDocument.getString("hours");
                        String details = requestDocument.getString("details");

                        // Populate request data
                        names.setText(employeesNames);
                        datesAndHours.setText(datesHours);
                        detailsText.setText(details);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}











/*

    void handleApproveAction() {
        try {
            Toast.makeText(this, "Approving request...", Toast.LENGTH_SHORT).show();
            if (requestDocument != null) {
                db.collection("Requests").document(requestDocument.getId())
                        .update("status", "approved")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Request Approved", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ManagerShiftChangeDialog.this, ManagerRequestPage.class);
                            intent.putExtra("LOGIN_EMAIL", managerEmail);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to approve request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Request document not loaded. Cannot approve.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error handling approve action: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void handleDenyAction() {
        try {
            Toast.makeText(this, "Denying request...", Toast.LENGTH_SHORT).show();
            if (requestDocument != null) {
                db.collection("Requests").document(requestDocument.getId())
                        .update("status", "denied")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Request Denied", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ManagerShiftChangeDialog.this, ManagerRequestPage.class);
                            intent.putExtra("LOGIN_EMAIL", managerEmail);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to deny request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Request document not loaded. Cannot deny.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error handling deny action: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
*/
