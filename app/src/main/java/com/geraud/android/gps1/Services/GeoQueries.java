package com.geraud.android.gps1.Services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class GeoQueries extends Service {

    public final IBinder iBinder = new LocalBinder();

    private long[] pattern = {2000, 1000, 500};

    private DatabaseReference mDatabaseReference;
    private GeoFire mGeoFire;
    private GeoQuery mGeoQuery;

    private Geocoder mGeocoder;
    private List<Address> mAddress;


    public GeoQueries() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //init variables
        mGeocoder = new Geocoder(GeoQueries.this, Locale.getDefault());
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("USER");
        mGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("LOCATION"));
    }

    //methods below
    public void query(LatLng geoLocation) {
        Toast.makeText(GeoQueries.this, "geolist is loaded", Toast.LENGTH_SHORT).show();
        //loop through the list of geo locations
        try {
            mAddress = mGeocoder.getFromLocation(geoLocation.latitude, geoLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(geoLocation.latitude, geoLocation.longitude), 0.2); // radius is in killometer 0.6 = 600 meters
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //read the users name from database using the key
                mDatabaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                User user = dc.getValue(User.class);
                                //make a notification to tell the user that his/her friend is in one of his perimeters
                                if (user != null) {
                                    PugNotification.with(GeoQueries.this)
                                            .load()
                                            .title("Geo Trigger")
                                            .message(String.format(Locale.getDefault(), "%s Just Entered Your GeoLocation At %s, %s",
                                                    user.getName().toUpperCase(), (mAddress.get(0).getFeatureName() != null) ? mAddress.get(0).getFeatureName() : "GeoLocation",
                                                    (mAddress.get(0).getLocality() != null) ? mAddress.get(0).getLocality() : "Area"))
                                            .flags(Notification.DEFAULT_ALL)
                                            .vibrate(pattern)
                                            .color(R.color.cornflower_blue)
                                            .custom()
                                            .build();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(GeoQueries.this, "GeoQueries On Exited Query Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                mDatabaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                User user = dc.getValue(User.class);
                                //make a notification to tell the user that his/her friend is in one of his perimeters
                                PugNotification.with(GeoQueries.this)
                                        .load()
                                        .title("Geo Trigger")
                                        .message(String.format(Locale.getDefault(), "%s Just Exited Your GeoLocation At %s, %s", user != null ? user.getName().toUpperCase() : null, (mAddress.get(0).getFeatureName() != null) ? mAddress.get(0).getFeatureName() : "GeoLocation", (mAddress.get(0).getLocality() != null) ? mAddress.get(0).getLocality() : "Area"))
                                        .flags(Notification.DEFAULT_ALL)
                                        .vibrate(pattern)
                                        .color(R.color.cornflower_blue)
                                        .custom()
                                        .build();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(GeoQueries.this, "GeoQueries On Exited Query Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Toast.makeText(getApplicationContext(), "GeoQuery- Started Sucessfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(getApplicationContext(), "GeoQuery- Start Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGeoQuery.removeAllListeners();
    }

    public class LocalBinder extends Binder {

        public GeoQueries getService() {

            return GeoQueries.this;
        }
    }
}
