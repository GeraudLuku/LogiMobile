package com.geraud.android.gps1.Utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.geraud.android.gps1.Chat.ChatActivity;
import com.geraud.android.gps1.R;
import com.google.firebase.messaging.RemoteMessage;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class BuildNotification {

    //constructor for Messages And LinkUP
    public BuildNotification(Context context, RemoteMessage rm){

        Intent intent = new Intent(context,ChatActivity.class);

        if (rm.getData().get("type").equals("single")){

            //create a pending intent
            intent.putExtra("chatId",rm.getData().get("chatId"));
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

            PugNotification.with(context)
                    .load()
                    .title(rm.getData().get("senderId"))
                    .message(rm.getData().get("message"))
                    .flags(Notification.FLAG_AUTO_CANCEL)
                    .click(pendingIntent)
                    .custom()
                    .build();
        }

        if (rm.getData().get("type").equals("multiple")){

            //create a pending Intent
            intent.putExtra("chatId",rm.getData().get("chatId"));
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);

            PugNotification.with(context)
                    .load()
                    .title(rm.getData().get("chatId"))
                    .message(String.format("%s : %s",rm.getData().get("senderId"),rm.getData().get("message")))
                    .flags(Notification.FLAG_AUTO_CANCEL)
                    .custom()
                    .build();

        }

        if (rm.getData().get("type").equals("linkUp")){

        }

        PugNotification.with(context)
                .load()
                .title(rm.getNotification().getTitle())
                .message(rm.getNotification().getBody())
                .flags(Notification.FLAG_AUTO_CANCEL)
                .custom()
                .build();
    }
}
