package com.geraud.android.gps1.Models;

import android.support.annotation.NonNull;

import com.geraud.android.gps1.Chat.ChatType;

import java.util.ArrayList;

public class Message extends ChatType {
    private String messageId,
            text,
            senderId;
    private long timestamp;
    private ArrayList<String> mediaUrlist;

    public Message(String messageId, String text, String senderId, long timestamp, ArrayList<String> mediaUrlist) {
        this.messageId = messageId;
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.mediaUrlist = mediaUrlist;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public ArrayList<String> getMediaUrlist() {
        return mediaUrlist;
    }

}
