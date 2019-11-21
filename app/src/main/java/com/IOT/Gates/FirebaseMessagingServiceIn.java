package com.IOT.Gates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingServiceIn extends FirebaseMessagingService {

    private final String TAG = "JSA-FCM";
    FirebaseAuth mAuth;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        Log.d(TAG, "title: " + remoteMessage.getData().get("title"));
        Log.d(TAG, "message: " + remoteMessage.getData().get("message"));
        //addNotification();
        sendBroadCastNotification(title, message);
        mAuth = FirebaseAuth.getInstance();

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onMessageSent(String msgId) {
        Log.e(TAG, "onMessageSent: " + msgId);
    }

    @Override
    public void onSendError(String msgId, Exception e) {
        Log.e(TAG, "onSendError: " + msgId);
        Log.e(TAG, "Exception: " + e);
    }

    void sendBroadCastNotification(String title, String message){
        //TODO find more about channel id
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "58");

        Intent notifyIntent = new Intent(this, MainActivity.class);

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this,0,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "com.iot.gates";
            String description = "some channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("58", name, importance);
            channel.setDescription(description); // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        builder.setContentTitle(title)
                .setPriority(2)
                .setContentText(message)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_code_scanner_flash_on)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.ic_code_scanner_auto_focus_off));

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.notify(9013, builder.build());
    }
}