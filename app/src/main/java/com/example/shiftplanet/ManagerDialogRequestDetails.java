package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManagerDialogRequestDetails extends AppCompatActivity {

    // UI Elements
    private ImageView backButton;
    private TextView requestDetailsTitle;
    private TextInputEditText requestDetailsContent;
    private Button approveButton;
    private Button denyButton;
    private Button downloadDocument;

    // Firestore instance and request document
    private FirebaseFirestore db;
    private DocumentSnapshot requestDocument;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_request_details);

        // Initialize UI elements
        backButton = findViewById(R.id.back_btn);
        requestDetailsContent = findViewById(R.id.request_details_content);
        approveButton = findViewById(R.id.approve_button);
        denyButton = findViewById(R.id.deny_button);
        downloadDocument = findViewById(R.id.add_document_button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch and save request document
        fetchRequestDocument(3, "orgoren3146@gmail.com", "orcallbiz1@gmail.com");

        // Set up back button functionality
        backButton.setOnClickListener(v -> finish());

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
                    .whereEqualTo("number", numberInput)
                    .whereEqualTo("managerEmail", managerEmail)
                    .whereEqualTo("employeeEmail", employeeEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                requestDocument = queryDocumentSnapshots.getDocuments().get(0); // Save the first matching document

                                try {
                                    String reason = requestDocument.getString("reason");
                                    String startDate = requestDocument.getString("startDate");
                                    String endDate = requestDocument.getString("endDate");
                                    String additionalDetails = requestDocument.getString("details");

                                    try {
                                        String formattedDetails = String.format(
                                                "Reason: %s\nStart Date: %s\nEnd Date: %s\nDetails: %s",
                                                reason != null ? reason : "N/A",
                                                startDate != null ? startDate : "N/A",
                                                endDate != null ? endDate : "N/A",
                                                additionalDetails != null ? additionalDetails : "N/A"
                                        );

                                        requestDetailsContent.setText(formattedDetails);
                                    } catch (Exception e) {
                                        Toast.makeText(this, "Error formatting details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(this, "Error retrieving fields from document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(this, "No matching request found.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Error processing query results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        startActivity(intent);
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
                        startActivity(intent);
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
