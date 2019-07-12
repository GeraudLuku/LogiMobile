package com.geraud.android.gps1.Models;

import java.io.Serializable;

public class ChatInfo implements Serializable {
    private String id,
            image,
            name,
            type;
    private Message lastMessage;

    public ChatInfo(){

    }

    public ChatInfo(String id, String image, String name, String type, Message lastMessage) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.type = type;
        this.lastMessage = lastMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}
