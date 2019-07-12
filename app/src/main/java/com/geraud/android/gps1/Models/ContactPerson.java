package com.geraud.android.gps1.Models;

public class ContactPerson {
    private String position;
    private long phone;

    public ContactPerson() {
    }

    public ContactPerson(String position, long phone) {
        this.position = position;
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }
}
