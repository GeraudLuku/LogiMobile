package com.geraud.android.gps1.Models;

import java.util.ArrayList;

public class Message {
   String messageId,
            text,
            senderId;
   ArrayList<String> mediaUrlist;

    public Message(String messageId, String text, String senderId, ArrayList<String> mediaUrlist) {
        this.messageId = messageId;
        this.text = text;
        this.senderId = senderId;
        this.mediaUrlist = mediaUrlist;
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
