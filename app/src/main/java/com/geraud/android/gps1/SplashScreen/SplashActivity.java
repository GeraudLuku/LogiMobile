package com.geraud.android.gps1.SplashScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.geraud.android.gps1.GoogleMap.MapsActivity;
import com.geraud.android.gps1.Onboarding_slides.Walkthrough;
import com.geraud.android.gps1.Registration;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.onesignal.OneSignal;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;

    //use this code for the logout button also in the future
    private void LogOut(){
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Registration.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialising variables
        FirebaseApp.initializeApp(this);
        Fresco.initialize(this);

        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);

        mFirebaseAuth = FirebaseAuth.getInstance();

        SharedPreferences mSharedPreferences = getSharedPreferences("mSharedPreferences", MODE_PRIVATE);
        boolean firstStart = mSharedPreferences.getBoolean("firstStart", true);

        //if its the first time the user starts the app send him to walkthrough activity
        if (firstStart) {
            startActivity(new Intent(this, Walkthrough.class)); //goto walkthrough
            //close splash activity
            finish();
        } else {
            //if its not the user first tym of starting the app but has not registered send them to registration activity
            //if a user is already signed in
            if (mFirebaseAuth.getCurrentUser() != null) {
                //if a user is signed in
                startActivity(new Intent(this, MapsActivity.class)); //goto main app screen
                //close splash activity
                finish();
            } else {
                //if no user is signed in send them to the registration page
                startActivity(new Intent(this, Registration.class)); //goto main app screen
                //close splash activity
                finish();
            }
        }

    }
}
