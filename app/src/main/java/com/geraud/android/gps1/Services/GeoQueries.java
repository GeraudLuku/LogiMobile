package com.geraud.android.gps1.Services;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import br.com.goncalves.pugnotification.notification.PugNotification;
import es.dmoral.toasty.Toasty;

public class GeoQueries extends Service {

    public final IBinder iBinder = new LocalBinder();

    private static final int INTERVAL = 60000; // one minute INTERVAL
    private long[] pattern = {2000, 1000, 500};

    private DatabaseReference mDatabaseReference;
    private GeoFire mGeoFire;
    private GeoQuery mGeoQuery;


    public GeoQueries() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    //methods below
    public void query(final Context context, final List<LatLng> GeoLocations, final List<String> Contacts) {

        //init varibles
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("USER");
        mGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("LOCATION"));

        //if the list of geo locations is not empty proceed as planned
        if (!GeoLocations.isEmpty()) {
            //run in a set  INTERVAL
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    //loop through the list of geo locations
                    for (final LatLng latlng : GeoLocations) {

                        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(latlng.latitude, latlng.longitude), 0.6); // radius is in killometer 0.6 = 600 meters
                        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                //check if the person is one of my users
                                if (!Contacts.isEmpty() && Contacts.contains(key)) {
                                    //read the users name from database using the key
                                    mDatabaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                                    User user = dc.getValue(User.class);
                                                    //make a notification to tell the user that his/her friend is in one of his perimeters
                                                    PugNotification.with(context)
                                                            .load()
                                                            .title("Geo Trigger")
                                                            .message(String.format("%s Just Entered Your GeoLocation At Lat : %d  Long : %d ", user.getName().toUpperCase(), latlng.latitude, latlng.longitude))
                                                            .flags(Notification.DEFAULT_ALL)
                                                            .vibrate(pattern)
                                                            .color(R.color.cornflower_blue)
                                                            .custom()
                                                            .build();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } //else do nothing
                            }

                            @Override
                            public void onKeyExited(String key) {

                                if (Contacts.contains(key)) {

                                    mDatabaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot dc : dataSnapshot.getChildren()) {
                                                    User user = dc.getValue(User.class);
                                                    //make a notification to tell the user that his/her friend is in one of his perimeters
                                                    PugNotification.with(context)
                                                            .load()
                                                            .title("Geo Trigger")
                                                            .message(String.format("%s Just Exited Your GeoLocation At Lat : %d  Long : %d ", user.getName().toUpperCase(), latlng.latitude, latlng.longitude))
                                                            .flags(Notification.DEFAULT_ALL)
                                                            .vibrate(pattern)
                                                            .color(R.color.cornflower_blue)
                                                            .custom()
                                                            .build();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location) {

                            }

                            @Override
                            public void onGeoQueryReady() {

                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error) {

                            }
                        });

                    }

                }
            }, 0, INTERVAL);

        } else
            Toast.makeText(context, "geolocation array is empty", Toast.LENGTH_SHORT).show();
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
