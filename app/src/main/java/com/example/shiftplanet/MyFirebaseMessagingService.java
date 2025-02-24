package com.example.shiftplanet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "ShiftPlanet_Notifications";
    private static String fcmToken;

    @Override
    public void onCreate() {
        super.onCreate();
        retrieveFCMToken();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);
        fcmToken = token; // Store the latest token
        saveTokenToFirestore(token);
    }

    private void retrieveFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        fcmToken = task.getResult();
                        Log.d(TAG, "FCM Token Retrieved: " + fcmToken);
                        saveTokenToFirestore(fcmToken);
                    } else {
                        Log.w(TAG, "Failed to get FCM Token", task.getException());
                    }
                });
    }

    private void saveTokenToFirestore(String token) {
        String userId = "USER_ID_HERE"; // Replace with the actual user ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", token);

        db.collection("users").document(userId)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token saved to Firestore"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save token", e));
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM Message Received: " + remoteMessage.getData());

        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "New Notification";
        String message = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "You have a new message.";

        sendNotification(title, message);
    }

    private void sendNotification(String title, String message) {
        createNotificationChannel();

        // Check if the app has the POST_NOTIFICATIONS permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission is not granted!");
                return; // Do not send notification without permission
            }
        }

        Intent intent = new Intent(this, EmployeeWorkArrangement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon) // Ensure you have a notification icon in `res/drawable`
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ShiftPlanet Notifications";
            String description = "Channel for ShiftPlanet app notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // 🔥 This function allows sending push notifications manually
    public static void sendPushNotification(String title, String message) {
        if (fcmToken == null) {
            Log.e(TAG, "FCM Token is null. Cannot send notification.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").add(new HashMap<String, Object>() {{
                    put("title", title);
                    put("message", message);
                    put("token", fcmToken);
                }}).addOnSuccessListener(aVoid -> Log.d(TAG, "Notification request added"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add notification request", e));
    }
}
