package com.geraud.android.gps1.Services;

import com.geraud.android.gps1.Utils.BuildNotification;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseCloudMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {

            new BuildNotification(getApplicationContext(), remoteMessage);

        }
    }
}
