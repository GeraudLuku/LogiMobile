package com.geraud.android.gps1.Services;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

public class OneSignal extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {
        //here i will customise the way a notification is been displayed

        return false;
    }
}
