package com.geraud.android.gps1.InfoWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.Place;
import com.geraud.android.gps1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;


public class PlacesMarkerInfoWindow implements GoogleMap.InfoWindowAdapter {

    private View mWindow;
    private Context mContext;

    public PlacesMarkerInfoWindow(Context mContext) {
        this.mContext = mContext;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.info_window_places, null);

    }

    //setting information onto the view
    private void renderWindowText(Marker marker, View v) {

        //get GSON and convert it
        Gson gson = new Gson();
        Place placeInfo = gson.fromJson(marker.getSnippet(), Place.class);

        //reference the title and snippetof the custom info Window
        TextView infotitle = v.findViewById(R.id.infoWindow_places_name);
        TextView infosnippet = v.findViewById(R.id.infoWindow_places_snippet);
        ImageView infoImage = v.findViewById(R.id.infoWindow_places_image);

        infotitle.setText(placeInfo.getName());
        infosnippet.setText(placeInfo.getDescription());
        Glide.with(mContext).load(placeInfo.getImage_uri()).into(infoImage);
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
