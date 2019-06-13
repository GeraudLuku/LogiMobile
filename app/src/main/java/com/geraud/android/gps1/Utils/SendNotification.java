package com.geraud.android.gps1.Utils;

import android.content.Context;
import android.widget.Toast;

import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class SendNotification {

    public SendNotification(final Context context, String message, String heading, String notificationKey) {

        try {
            JSONObject notificationContent = new JSONObject(
                            "{'contents':{'en':'" + message + "'}," +
                            "'include_player_ids':['" + notificationKey + "']," +
                            "'headings':{'en': '" + heading + "'}}");
            OneSignal.postNotification(notificationContent, new OneSignal.PostNotificationResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Toast.makeText(context,"sent Push Notification",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(JSONObject response) {
                    Toast.makeText(context,"Failed to send Push Notification",Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}