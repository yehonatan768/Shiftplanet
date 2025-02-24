package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationDetailActivityPage extends AppCompatActivity {

    private FirebaseFirestore db;
    private String notificationId; // The ID of the notification
    private ImageView backButton;
    private String employeeEmail;
    private String className;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the email
        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            employeeEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }

        // Get the notification ID passed from previous activity
        notificationId = getIntent().getStringExtra("notificationId");
        className = getIntent().getStringExtra("CLASS_NAME");

        if (notificationId != null) {
            fetchNotificationDetails(notificationId);
        } else {
            Toast.makeText(this, "Notification ID not found.", Toast.LENGTH_SHORT).show();
        }

        // Initialize the backButton here, outside of the listener
        backButton = findViewById(R.id.btnBackND);

        // Check user type (manager or employee) inside the onCreate method
        db.collection("users").whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming only one document is found
                        String userType = queryDocumentSnapshots.getDocuments().get(0).getString("userType");

                        // Set the back button action based on user type
                        backButton.setOnClickListener(v -> {
                            Intent intent;
                            if ("Manager".equalsIgnoreCase(userType)) {
                                intent = new Intent(NotificationDetailActivityPage.this, ManagerSentNotificationsPage.class);
                            } else if ("Employee".equalsIgnoreCase(userType)) {
                                if("EmployeeNotificationsPage".equalsIgnoreCase(className)) {
                                    intent = new Intent(NotificationDetailActivityPage.this, EmployeeNotificationsPage.class);
                                }
                                else{ intent = new Intent(NotificationDetailActivityPage.this, EmployeeHomePage.class);}


                            } else {
                                // Default case if user type is unknown
                                Toast.makeText(NotificationDetailActivityPage.this, "Usertype unknown.", Toast.LENGTH_SHORT).show();
                                intent = new Intent(NotificationDetailActivityPage.this, Login.class); // Replace with appropriate fallback
                            }
                            intent.putExtra("LOGIN_EMAIL", employeeEmail);  // Pass the email back to the next activity
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        Toast.makeText(NotificationDetailActivityPage.this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NotificationDetailActivityPage.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchNotificationDetails(String notificationId) {
        // Fetch the notification details from Firestore
        db.collection("Notifications")
                .document(notificationId)  // Using the notification ID passed from the previous activity
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the title, message, and timestamp fields from the Firestore document
                        String title = documentSnapshot.getString("title");  // Assuming "title" is the correct field name
                        String message = documentSnapshot.getString("notification");  // Assuming "notification" is the correct field name
                        com.google.firebase.Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");  // Use Timestamp

                        // Set the title if available
                        if (title != null) {
                            TextView titleTextView = findViewById(R.id.titleTextView);
                            titleTextView.setText(title);
                        } else {
                            Toast.makeText(NotificationDetailActivityPage.this, "Title not found.", Toast.LENGTH_SHORT).show();
                        }

                        // Set the message if available
                        if (message != null) {
                            TextView messageTextView = findViewById(R.id.messageTextView);
                            messageTextView.setText(message);
                        } else {
                            Toast.makeText(NotificationDetailActivityPage.this, "Message not found.", Toast.LENGTH_SHORT).show();
                        }

                        // Process timestamp to show in a readable format
                        if (timestamp != null) {
                            // Convert Timestamp to Date
                            Date date = timestamp.toDate();  // Convert to Date object
                            String readableDate = convertDateToString(date);  // Convert Date to string
                            TextView dateTextView = findViewById(R.id.dateTextView);
                            dateTextView.setText(readableDate);
                        } else {
                            Toast.makeText(NotificationDetailActivityPage.this, "Timestamp not found.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(NotificationDetailActivityPage.this, "Notification not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NotificationDetailActivityPage.this, "Error fetching notification.", Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to convert Date to readable string format
    private String convertDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

}