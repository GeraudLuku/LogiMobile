package com.geraud.android.gps1.InfoWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.geraud.android.gps1.Models.Branch;
import com.geraud.android.gps1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

public class BranchMarkerInfoWindow implements GoogleMap.InfoWindowAdapter {

    private View mWindow;
    private Context mContext;

    public BranchMarkerInfoWindow(Context mContext) {
        this.mContext = mContext;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.info_window_branch, null);

    }

    //setting information onto the view
    private void renderWindowText(Marker marker, View v) {

        //get GSON and convert it
        Gson gson = new Gson();
        Branch branch = gson.fromJson(marker.getSnippet(), Branch.class);


        //reference the title and snippetof the custom info Window
        TextView infotitle = v.findViewById(R.id.name);
        TextView infosnippet = v.findViewById(R.id.address);

        infotitle.setText(branch.getName());
        infosnippet.setText(branch.getPostalAddress());

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
