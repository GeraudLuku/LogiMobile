package com.geraud.android.gps1.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Stories implements Serializable {
    private Boolean location;
    private String media, description;
    private long timestamp;
    private Double latitude, longitude;
    private String type;
    private String phone;


    private ArrayList<Stories> storyObjectArrayList = new ArrayList<>();

    public Stories() {
        //empty constructor
    }

    public Stories(Boolean location, String media, String description, long timestamp, Double latitude, Double longitude, String type, String phone) {
        this.location = location;
        this.media = media;
        this.description = description;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.phone = phone;
    }

    public Boolean getLocation() {
        return location;
    }

    public void setLocation(Boolean location) {
        this.location = location;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String media) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addStoryToArray(Stories mStories) {
        storyObjectArrayList.add(mStories);
    }

    public ArrayList<Stories> getStoryObjectArrayList() {
        return storyObjectArrayList;
    }

    public int getCount() {
        return storyObjectArrayList.size();
    }
}
