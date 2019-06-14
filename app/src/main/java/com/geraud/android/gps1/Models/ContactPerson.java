package com.geraud.android.gps1.Models;

public class ContactPerson {
    private String position
            ,phone;

    public ContactPerson(String position, String phone) {
        this.position = position;
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
