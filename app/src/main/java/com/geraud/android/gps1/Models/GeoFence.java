package com.geraud.android.gps1.Models;

/**
 * Created by Geraud on 7/19/2018.
 */

public class GeoFence {
    private double latitude, longitude;

    public GeoFence() {
    }

    public GeoFence(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
