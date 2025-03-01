package com.example.shiftplanet;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.HashMap;
import java.util.Map;

public class ManagerSendNotificationPage extends AppCompatActivity {
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private int businessCode;
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String managerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_send_notification_page);

        managerEmail = getIntent().getStringExtra("LOGIN_EMAIL");

        if (managerEmail != null) {
            Log.d(TAG, "Received email: " + managerEmail);
        } else {
            Log.e(TAG, "No email received");
        }

        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.manager_send_notification_toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.manager_send_notification);
        navigationView = findViewById(R.id.manager_send_notification_nav_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item);
            return true;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Button sendNotificationButton = findViewById(R.id.send_notification_btn);
        TextInputEditText notificationEditText = findViewById(R.id.notification_txt);
        TextInputEditText titleEditText = findViewById(R.id.title_txt);

        sendNotificationButton.setOnClickListener(v -> {
            String notification = notificationEditText.getText().toString();
            String title = titleEditText.getText().toString();

            db.collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        businessCode = Integer.parseInt(document.getString("businessCode"));
                        sendNotification(title,notification, businessCode, managerEmail);
                    } else {
                        Log.e("FirestoreError", "Failed to fetch manager's email", task.getException());
                        Toast.makeText(ManagerSendNotificationPage.this, "Failed to retrieve manager's email.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("FirestoreError", "Error getting user document", task.getException());
                }
            });
        });
    }

    private void sendNotification(String title, String notification, int businessCode, String managerEmail) {
        getNextNotificationNumber(notificationNumber -> {
            if (notificationNumber == -1) {
                Toast.makeText(ManagerSendNotificationPage.this, "Error generating notification number", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> managerNotification = new HashMap<>();
            managerNotification.put("title", title);
            managerNotification.put("notification", notification);
            managerNotification.put("businessCode", businessCode);
            managerNotification.put("managerEmail", managerEmail);
            managerNotification.put("timestamp", FieldValue.serverTimestamp());
            managerNotification.put("notificationId", notificationNumber);

            db.collection("Notifications")
                    .add(managerNotification)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(ManagerSendNotificationPage.this, "Notification published successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ManagerSendNotificationPage.this, "Error publishing notification", Toast.LENGTH_SHORT).show();
                    });
        });
    }



    private void getNextNotificationNumber(OnNotificationNumberGeneratedListener listener) {
        db.collection("RequestCounters").document("NotificationsCounter")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("RequestCounters").document("NotificationsCounter")
                                .update("counter", FieldValue.increment(1))
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        db.collection("RequestCounters").document("NotificationsCounter")
                                                .get()
                                                .addOnSuccessListener(document -> {
                                                    long counter = document.getLong("counter");
                                                    listener.onNotificationNumberGenerated(counter);
                                                });
                                    } else {
                                        listener.onNotificationNumberGenerated(-1);
                                    }
                                });
                    } else {
                        db.collection("RequestCounters").document("NotificationsCounter")
                                .set(new HashMap<String, Object>() {{
                                    put("counter", 1);
                                }}, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> listener.onNotificationNumberGenerated(1))
                                .addOnFailureListener(e -> listener.onNotificationNumberGenerated(-1));
                    }
                });
    }


    interface OnNotificationNumberGeneratedListener {
            void onNotificationNumberGenerated(long notificationNumber);
        }

    private void handleNavigationItemSelected(MenuItem item) {
        Intent intent = null;
        if (item.getItemId() ==  R.id.m_home_page) {
            Toast.makeText(ManagerSendNotificationPage.this, "Home Page clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSendNotificationPage.this, ManagerHomePage.class);
        } else if (item.getItemId() == R.id.m_my_profile) {
            Toast.makeText(ManagerSendNotificationPage.this, "My profile clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSendNotificationPage.this, ManagerProfile.class);
        } else if (item.getItemId() == R.id.employees_requests) {
            Toast.makeText(ManagerSendNotificationPage.this, "Employees requests clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSendNotificationPage.this, ManagerRequestPage.class);
        } else if (item.getItemId() == R.id.build_work_arrangement) {
            Toast.makeText(ManagerSendNotificationPage.this, "Build work arrangement clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSendNotificationPage.this, ManagerWorkArrangement.class);
        }  else if (item.getItemId() == R.id.send_notifications) {
            Toast.makeText(ManagerSendNotificationPage.this, "Send notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSendNotificationPage.this, ManagerSendNotificationPage.class);
        } else if (item.getItemId() == R.id.sent_notifications) {
            Toast.makeText(ManagerSendNotificationPage.this, "Sent notifications clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSendNotificationPage.this, ManagerSentNotificationsPage.class);
        } else if (item.getItemId() == R.id.m_log_out) {
            Toast.makeText(ManagerSendNotificationPage.this, "Log out clicked", Toast.LENGTH_SHORT).show();
            intent = new Intent(ManagerSendNotificationPage.this, Login.class);
        }

        if (intent != null) {
            intent.putExtra("LOGIN_EMAIL", managerEmail);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);


    }
}
