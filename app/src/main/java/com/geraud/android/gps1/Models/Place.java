package com.geraud.android.gps1.Models;

/**
 * Created by Geraud on 6/21/2018.
 */

public class Place {
    private String name, creator, description, image_uri, type, desc2;
    private double latitude, longitude;

    public Place() {
    }

    public Place(String name, String creator, String description, String image_uri, String type, double latitude, double longitude, String des2) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.image_uri = image_uri;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.desc2 = des2;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
