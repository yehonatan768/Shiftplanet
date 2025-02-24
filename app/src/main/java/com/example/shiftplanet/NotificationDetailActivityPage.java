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
    private String notificationId;
    private ImageView backButton;
    private String employeeEmail;
    private String className;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);


        db = FirebaseFirestore.getInstance();


        String email = getIntent().getStringExtra("LOGIN_EMAIL");

        if (email != null) {
            Log.d(TAG, "Received email: " + email);
            employeeEmail = email;
        } else {
            Log.e(TAG, "No email received");
        }


        notificationId = getIntent().getStringExtra("notificationId");
        className = getIntent().getStringExtra("CLASS_NAME");

        if (notificationId != null) {
            fetchNotificationDetails(notificationId);
        } else {
            Toast.makeText(this, "Notification ID not found.", Toast.LENGTH_SHORT).show();
        }

        backButton = findViewById(R.id.btnBackND);

        db.collection("users").whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userType = queryDocumentSnapshots.getDocuments().get(0).getString("userType");

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
                                Toast.makeText(NotificationDetailActivityPage.this, "Usertype unknown.", Toast.LENGTH_SHORT).show();
                                intent = new Intent(NotificationDetailActivityPage.this, Login.class);
                            }
                            intent.putExtra("LOGIN_EMAIL", employeeEmail);
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

        db.collection("Notifications")
                .document(notificationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String message = documentSnapshot.getString("notification");
                        com.google.firebase.Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");

                        if (title != null) {
                            TextView titleTextView = findViewById(R.id.titleTextView);
                            titleTextView.setText(title);
                        } else {
                            Toast.makeText(NotificationDetailActivityPage.this, "Title not found.", Toast.LENGTH_SHORT).show();
                        }

                        if (message != null) {
                            TextView messageTextView = findViewById(R.id.messageTextView);
                            messageTextView.setText(message);
                        } else {
                            Toast.makeText(NotificationDetailActivityPage.this, "Message not found.", Toast.LENGTH_SHORT).show();
                        }

                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            String readableDate = convertDateToString(date);
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

    private String convertDateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

}