package com.geraud.android.gps1.InfoWindow;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.geraud.android.gps1.Models.User;
import com.geraud.android.gps1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;


public class PeopleMarkerInfoWindow implements GoogleMap.InfoWindowAdapter {

    private View mWindow;
    private Context mContext;

    public PeopleMarkerInfoWindow(Context mContext) {
        this.mContext = mContext;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.info_window_people, null);

    }

    //setting information onto the view
    private void renderWindowText(Marker marker, View v) {

        //get GSON and convert it
        Gson gson = new Gson();
        User userInfo = gson.fromJson(marker.getSnippet(), User.class);


        //reference the title and snippetof the custom info Window
        TextView infotitle = v.findViewById(R.id.infowindow_people_name);
        TextView infosnippet = v.findViewById(R.id.infowindow_people_snippet);
        ImageView infoimage = v.findViewById(R.id.infowindow_people_Image);

        infotitle.setText(userInfo.getName());

        //[0] online [1] offline [2] away
        if (userInfo.getStatus() == 0) {
            infosnippet.setTextColor(Color.GREEN);
            infosnippet.setText("ONLINE");
        } else if (userInfo.getStatus() == 1) {
            infosnippet.setTextColor(Color.RED);
            infosnippet.setText("OFFLINE");
        } else if (userInfo.getStatus() == 2) {
            infosnippet.setTextColor(Color.YELLOW);
            infosnippet.setText("AWAY");
        }
        //glide to set image on the image view of the info window
        Glide.with(mContext)
                .load(userInfo.getImage_uri())
                .into(infoimage);

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
