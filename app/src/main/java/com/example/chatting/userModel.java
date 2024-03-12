package com.example.chatting;

import java.io.Serializable;

public class userModel implements Serializable {
    public userModel(){}
    public userModel(String userId, String displayName, String photoUrl) {
        this.userId = userId;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    private String userId;
    private String displayName;
    private String photoUrl;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
