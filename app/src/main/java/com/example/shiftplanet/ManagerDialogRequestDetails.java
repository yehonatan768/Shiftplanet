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

    private ImageView backButton;
    private TextView requestDetailsContent;
    private Button approveButton;
    private Button denyButton;
    private Button downloadDocument;

    protected String managerEmail;
    private String employeeEmail;
    protected FirebaseFirestore db;
    protected DocumentSnapshot requestDocument;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_request_details);

        managerEmail = Objects.requireNonNull(getIntent().getStringExtra("managerEmail")).trim();
        employeeEmail = Objects.requireNonNull(getIntent().getStringExtra("employeeEmail")).trim();
        int requestNumber = getIntent().getIntExtra("requestNumber", -1);

        if (requestNumber != -1) {
            Log.d(TAG, "Request Number: " + requestNumber);
        } else {
            Toast.makeText(this, "Invalid request number: " + requestNumber, Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("TestDebug", "Manager Email: " + getIntent().getStringExtra("managerEmail"));
        Log.d("TestDebug", "Employee Email: " + getIntent().getStringExtra("employeeEmail"));
        Log.d("TestDebug", "Request Number: " + getIntent().getIntExtra("requestNumber", -1));

        Log.d(TAG, "Manager Email: " + managerEmail);
        Log.d(TAG, "Employee Email: " + employeeEmail);
        Log.d(TAG, "Request Number: " + requestNumber);

        backButton = findViewById(R.id.btnBackDialog);
        requestDetailsContent = findViewById(R.id.details_text);
        approveButton = findViewById(R.id.approve_button);
        denyButton = findViewById(R.id.deny_button);
        downloadDocument = findViewById(R.id.add_document_button);

        db = FirebaseFirestore.getInstance();

        fetchRequestDocument(requestNumber, managerEmail, employeeEmail);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDialogRequestDetails.this, ManagerRequestPage.class);
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            startActivity(intent);
            finish();
        });

        approveButton.setOnClickListener(v -> handleApproveAction());
        denyButton.setOnClickListener(v -> handleDenyAction());
        downloadDocument.setOnClickListener(v -> handleAddDocumentAction());
    }

    private void fetchRequestDocument(int numberInput, String managerEmail, String employeeEmail) {
        try {
            db.collection("Requests")
                    .whereEqualTo("requestNumber", numberInput)
                    .whereEqualTo("managerEmail", managerEmail)
                    .whereEqualTo("employeeEmail", employeeEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                requestDocument = queryDocumentSnapshots.getDocuments().get(0);

                                db.collection("users")
                                        .whereEqualTo("email", employeeEmail.trim())
                                        .get()
                                        .addOnSuccessListener(querySnapshots -> {
                                            Log.d(TAG, "Query succeeded. Number of matching documents: " + querySnapshots.size());
                                            if (!querySnapshots.isEmpty()) {
                                                DocumentSnapshot document = querySnapshots.getDocuments().get(0);
                                                String fullname = document.getString("fullname");
                                                Log.d(TAG, "Retrieved fullname: " + fullname);

                                                runOnUiThread(() -> {
                                                    TextView employeeNameView = findViewById(R.id.employee_name);
                                                    if (fullname != null && !fullname.isEmpty()) {
                                                        employeeNameView.setText(fullname);
                                                    } else {
                                                        employeeNameView.setText("N/A");
                                                        Log.d(TAG, "Fullname field is null or empty.");
                                                    }
                                                });
                                            } else {
                                                Log.d(TAG, "No documents found for email: " + employeeEmail);
                                                runOnUiThread(() -> {
                                                    TextView employeeNameView = findViewById(R.id.employee_name);
                                                    employeeNameView.setText("N/A");
                                                });
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Firestore query failed: " + e.getMessage()));

                                String requestType = requestDocument.getString("requestType");
                                String startDate = requestDocument.getString("startDate");
                                String endDate = requestDocument.getString("endDate");
                                String additionalDetails = requestDocument.getString("details");

                                try {
                                    TextView datesView = findViewById(R.id.dates);
                                    datesView.setText(String.format("%s - %s", startDate, endDate));
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating dates view: " + e.getMessage());
                                }

                                try {
                                    TextView requestTypeView = findViewById(R.id.requestTypeText);
                                    requestTypeView.setText(requestType);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating request type view: " + e.getMessage());
                                }

                                try {
                                    TextView detailsView = findViewById(R.id.details_text);
                                    detailsView.setText(additionalDetails);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating details view: " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing request document: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch request details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(this, "Error initiating Firestore query: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void handleApproveAction() {
        try {
            Toast.makeText(this, "Approving request...", Toast.LENGTH_SHORT).show();
            if (requestDocument != null) {
                db.collection("Requests").document(requestDocument.getId())
                        .update("status", "approved")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Request Approved", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ManagerDialogRequestDetails.this, ManagerRequestPage.class);
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
                            Intent intent = new Intent(ManagerDialogRequestDetails.this, ManagerRequestPage.class);
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

    private void handleAddDocumentAction() {
        try {
            Toast.makeText(this, "Downloading Document...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error handling add document action: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
