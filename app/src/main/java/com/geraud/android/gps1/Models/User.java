package com.geraud.android.gps1.Models;

import java.io.Serializable;

public class User implements Serializable {
    private String name, type, image_uri, phone, notificationKey;
    private Integer status; // (0 for online 1 for offline 2 for away)
    private Boolean selected;

    public User(){
        // a must add
    }
    public User(String phone) {
        this.phone = phone;
    }

    public User(String name, String type, String image_uri, Integer status, String phone, String notificationKey) {
        this.name = name;
        this.type = type;
        this.image_uri = image_uri;
        this.status = status;
        this.phone = phone;
        this.notificationKey = notificationKey;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public Integer getStatus() {
        return status;
    }

    public String getPhone() {
        return phone;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
