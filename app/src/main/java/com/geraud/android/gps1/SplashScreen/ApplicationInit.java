package com.geraud.android.gps1.SplashScreen;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.geraud.android.gps1.OneSignal.NotificationOpenHandler;
import com.google.firebase.FirebaseApp;
import com.onesignal.OneSignal;

//this application is called before any other class or service is called it basically it initialises the application
public class ApplicationInit extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //initialising variables
        FirebaseApp.initializeApp(this);
        Fresco.initialize(this);
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationOpenHandler(this))
                .init();
        OneSignal.setSubscription(true);
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

    }
}