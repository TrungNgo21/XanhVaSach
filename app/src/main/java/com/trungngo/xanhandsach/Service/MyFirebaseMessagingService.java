package com.trungngo.xanhandsach.Service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.trungngo.xanhandsach.Activity.MainActivity;
import com.trungngo.xanhandsach.R;
import com.trungngo.xanhandsach.Shared.Constant;
import com.trungngo.xanhandsach.Shared.PreferenceManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

  private final UserService userService = new UserService(this);

  @Override
  public void onNewToken(@NonNull String token) {
    super.onNewToken(token);
    FirebaseMessaging.getInstance()
        .getToken()
        .addOnCompleteListener(
            tokenTask -> {
              if (tokenTask.isSuccessful()) {
                String fcmToken = tokenTask.getResult();
                FirebaseFirestore.getInstance()
                    .collection(Constant.KEY_COLLECTION_USERS)
                    .document(userService.getCurrentUser().getId())
                    .update("fcmToken", fcmToken)
                    .addOnCompleteListener(
                        task -> {
                          if (task.isSuccessful()) {
                            FirebaseFirestore.getInstance()
                                .collection(Constant.KEY_COLLECTION_SITES)
                                .document(userService.getCurrentUser().getSiteId())
                                .update("owner.fcmToken", fcmToken)
                                .addOnCompleteListener(
                                    updateTokenTask -> {
                                      if (updateTokenTask.isSuccessful()) {}
                                    });
                          }
                        });
                if (userService.getCurrentUser().getJoinSiteId() != null) {
                  FirebaseFirestore.getInstance()
                      .collection(Constant.KEY_COLLECTION_SITES)
                      .document();
                }
              }
            });
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

    super.onMessageReceived(remoteMessage);
    Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"));
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
    String channelId = "Default";
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(remoteMessage.getNotification().getTitle())
            .setContentText(remoteMessage.getNotification().getBody())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
    ;
    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(
              channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
      manager.createNotificationChannel(channel);
    }
    manager.notify(0, builder.build());
  }
}
