package com.geraud.android.gps1.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.onesignal.OneSignal;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class SendNotification {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final String LEGACY_SERVER_KEY = "AIzaSyCcajEcOEV-Dhw3dKZrOO3V121SXLPo5DM";
    private String TAG = "Sending Notification";

    //constructor for Messages and LinkUp
    public SendNotification(final String regToken, final String chatId , final String message , final String senderId , final String type, final String lat , final String lng) {
        new  AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json =new JSONObject();
                    JSONObject dataJson =new JSONObject();
                    dataJson.put("chatId",chatId);
                    dataJson.put("message",message);
                    dataJson.put("senderId",senderId);
                    dataJson.put("type",type);
                    dataJson.put("lat",lat);
                    dataJson.put("lng",lng);
                    json.put("notification",dataJson);
                    json.put("to",regToken);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization","key="+LEGACY_SERVER_KEY)
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                }catch (Exception e){
                    Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }
}
