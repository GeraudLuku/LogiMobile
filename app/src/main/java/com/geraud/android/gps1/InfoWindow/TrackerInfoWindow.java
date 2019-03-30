package com.geraud.android.gps1.InfoWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;


public class TrackerInfoWindow implements GoogleMap.InfoWindowAdapter {

    private View mWindow;
    private Context mContext;

    public TrackerInfoWindow(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.info_window_tracker, null);

    }

    //setting information onto the view
    private void renderWindowText(Marker marker, View v) {

        //get GSON and convert it
        Gson gson = new Gson();
        User userInfo = gson.fromJson(marker.getSnippet(), User.class);


        TextView infotitle = v.findViewById(R.id.trackerId);
        Chronometer chronometer = v.findViewById(R.id.chronometer);

        infotitle.setText(userInfo.getName());
        chronometer.start();



    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}
