package com.example.shiftplanet;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
public class NotificationDetailActivityPage extends AppCompatActivity {

    private FirebaseFirestore db;
    private String notificationId; // The ID of the notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the notification ID passed from previous activity
        notificationId = getIntent().getStringExtra("notificationId");

        if (notificationId != null) {
            fetchNotificationDetails(notificationId);
        } else {
            Toast.makeText(this, "Notification ID not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchNotificationDetails(String notificationId) {
        // Fetch the notification details from Firestore
        db.collection("Notifications")
                .document(notificationId)  // Using the notification ID passed from the previous activity
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the message field from the Firestore document
                        String message = documentSnapshot.getString("notification");  // Assuming "message" is the correct field name

                        // If the message is available, set it in the TextView
                        if (message != null) {
                            TextView messageTextView = findViewById(R.id.messageTextView);
                            messageTextView.setText(message);
                        } else {
                            Toast.makeText(NotificationDetailActivityPage.this, "Message not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(NotificationDetailActivityPage.this, "Notification not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NotificationDetailActivityPage.this, "Error fetching notification.", Toast.LENGTH_SHORT).show();
                });
    }
}
