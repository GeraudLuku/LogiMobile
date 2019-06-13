package com.geraud.android.gps1.OneSignal;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.geraud.android.gps1.Chat.ChatsActivity;
import com.geraud.android.gps1.Models.PromotionMessage;
import com.geraud.android.gps1.PromotionMessage.PromotionMessageActivity;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class NotificationOpenHandler implements OneSignal.NotificationOpenedHandler {

    private Application application;

    public NotificationOpenHandler(Application application) {
        this.application = application;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        JSONObject data = result.notification.payload.additionalData;
        String title = result.notification.payload.title;
        String body = result.notification.payload.body;

        PromotionMessage promotionMessage = new PromotionMessage(title, body
                , data.optString("branchId", "noBranchIdGotten")
                , data.optLong("timestamp", 0)
                , data.optString("companyId", "noCompanyId"));

        if (data != null) {
            //send to view promtion message
            Intent intent = new Intent(application, PromotionMessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putSerializable("promotionMessage", promotionMessage);
            intent.putExtras(bundle);
            application.startActivity(intent);
        } else {
            //send to chats activity
            application.startActivity(new Intent(application, ChatsActivity.class));
        }
    }
}
