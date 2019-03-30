package com.geraud.android.gps1.Models;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    private String chatId;
    private Boolean selected = false;
    private ArrayList<User> userObjectArrayList = new ArrayList<>();

    public Chat(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public ArrayList<User> getUserObjectArrayList() {
        return userObjectArrayList;
    }

    //add a user object to array list
    public void addUserToArrayList(User mUser){
        userObjectArrayList.add(mUser);
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
