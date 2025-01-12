package application.lemontree.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import application.lemontree.R;
import application.lemontree.activities.MessageActivity;
import application.lemontree.activities.OfferDetailActivity;

// based on Google Android Example: https://github.com/firebase/quickstart-android/blob/master/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/java/MyFirebaseMessagingService.java

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "inMyFirebaseMessagingService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG,
                    "Message Notification data payload: " + remoteMessage.getData().toString());
            if (remoteMessage.getNotification().getBody() != null) {
                sendNotification(remoteMessage);
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("profiles").document(currentUser.getUid());

            userRef.update("FCMtoken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated successfully"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating token", e));
        }
    }
    // [END on_new_token]

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage FCM message
     */
    public void sendNotification(RemoteMessage remoteMessage) {

        String type = remoteMessage.getData().get("type");  // Extract offerId
        Log.d("FCM", type);
        Intent intent;

        if (type.equals("offer")) {
            String offerId = remoteMessage.getData().get("offerId");
            Log.d("FCM", "Offer ID received: " + offerId);
            intent = new Intent(this, OfferDetailActivity.class);
            intent.putExtra("offerId", offerId);
        } else {
            intent = new Intent(this, MessageActivity.class);
        }

        // Create PendingIntent with appropriate flags
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.lemonicon)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Get NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For Android Oreo and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build());
    }
}
