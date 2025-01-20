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

public class ManagerDialogRequestDetails extends AppCompatActivity {

    // UI Elements
    private ImageView backButton;
    private TextView requestDetailsContent;
    private Button approveButton;
    private Button denyButton;
    private Button downloadDocument;

    private String managerEmail;
    private String employeeEmail;
    // Firestore instance and request document
    private FirebaseFirestore db;
    private DocumentSnapshot requestDocument;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_request_details);

        managerEmail = Objects.requireNonNull(getIntent().getStringExtra("managerEmail")).trim();
        employeeEmail = Objects.requireNonNull(getIntent().getStringExtra("employeeEmail")).trim();
        int requestNumber = getIntent().getIntExtra("requestNumber", - 1);

        if (requestNumber != -1) {
            Log.d(TAG, "Request Number: " + requestNumber);
        } else {
            Toast.makeText(this, "Invalid request number: " + requestNumber, Toast.LENGTH_SHORT).show();
            return; // Exit to avoid further issues
        }

        Log.d(TAG, "Manager Email: " + managerEmail);
        Log.d(TAG, "Employee Email: " + employeeEmail);
        Log.d(TAG, "Request Number: " + requestNumber);

        // Initialize UI elements
        backButton = findViewById(R.id.btnBackDialog);
        requestDetailsContent = findViewById(R.id.details_text);
        approveButton = findViewById(R.id.approve_button);
        denyButton = findViewById(R.id.deny_button);
        downloadDocument = findViewById(R.id.add_document_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch and save request document
        fetchRequestDocument(requestNumber, managerEmail, employeeEmail);

        // Set up back button functionality
        backButton.findViewById(R.id.back_btn).setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDialogRequestDetails.this, ManagerRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            startActivity(intent);
            finish();
        });

        // Approve button click listener
        approveButton.setOnClickListener(v -> {
            handleApproveAction();
        });

        // Deny button click listener
        denyButton.setOnClickListener(v -> {
            handleDenyAction();
        });

        // Add document button click listener
        downloadDocument.setOnClickListener(v -> {
            handleAddDocumentAction();
        });
    }
    // Fetch request document from Firestore
    private void fetchRequestDocument(int numberInput, String managerEmail, String employeeEmail) {
        try {
            db.collection("Requests")
                    .whereEqualTo("requestNumber", numberInput)
                    .whereEqualTo("managerEmail", managerEmail)
                    .whereEqualTo("employeeEmail", employeeEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // Fetch employee name from user collection
                        db.collection("Users")
                                .whereEqualTo("email", employeeEmail)
                                .get()
                                .addOnSuccessListener(querySnapshots -> {
                                    if (!querySnapshots.isEmpty()) {
                                        String fullname = querySnapshots.getDocuments().get(0).getString("fullname");
                                        TextView employeeNameView = findViewById(R.id.employee_name);
                                        employeeNameView.setText(fullname != null ? fullname : "N/A");
                                    } else {
                                        Log.d(TAG, "Employee not found");
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error fetching employee name: " + e.getMessage()));

                        // Populate request data
                        if (requestDocument != null) {
                            String reason = requestDocument.getString("reason");
                            String startDate = requestDocument.getString("startDate");
                            String endDate = requestDocument.getString("endDate");
                            String additionalDetails = requestDocument.getString("details");

                            TextView datesView = findViewById(R.id.dates);
                            datesView.setText(String.format("%s - %s", startDate, endDate));

                            TextView requestTypeView = findViewById(R.id.requestTypeText);
                            requestTypeView.setText(reason);

                            TextView detailsView = findViewById(R.id.details_text);
                            detailsView.setText(additionalDetails);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch request details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error initiating Firestore query: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // Handle approve action
    private void handleApproveAction() {
        if (requestDocument != null) {
            db.collection("Requests").document(requestDocument.getId())
                    .update("status", "approved")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Request Approved", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ManagerDialogRequestDetails.this, ManagerRequestPage.class); // Redirect to login
                        intent.putExtra("LOGIN_EMAIL", managerEmail);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to approve request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Request document not loaded. Cannot approve.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle deny action
    private void handleDenyAction() {
        if (requestDocument != null) {
            db.collection("Requests").document(requestDocument.getId())
                    .update("status", "denied")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Request Denied", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ManagerDialogRequestDetails.this, ManagerRequestPage.class); // Redirect to login
                        intent.putExtra("LOGIN_EMAIL", managerEmail);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to deny request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Request document not loaded. Cannot deny.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle add document action
    private void handleAddDocumentAction() {
        Toast.makeText(this, "Downloading Document...", Toast.LENGTH_SHORT).show();
        // TODO: Implement actual document download logic
    }
}
