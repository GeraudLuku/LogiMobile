package com.geraud.android.gps1.Chat;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

/**
 * this is an extendebale class which gets the ID of the blog post from home fragment  passed into it and can then be used
 * by other classes
 */

public class ChatType {

    @Exclude
    public String mChatType;

    public <T extends ChatType> T withType(@NonNull final String type) {
        this.mChatType = type;
        return (T) this;
    }


}
