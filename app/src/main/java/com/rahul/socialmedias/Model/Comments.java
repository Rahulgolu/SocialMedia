package com.rahul.socialmedias.Model;

public class Comments {

    private String userId;
    private String username;
    private String profilePic;
    private String textcmt;
    private long timestamp;

    public Comments() {
    }

    public Comments(String userId, String username, String profilePic, String textcmt, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.profilePic = profilePic;
        this.textcmt = textcmt;
        this.timestamp = timestamp;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getTextcmt() {
        return textcmt;
    }

    public void setTextcmt(String textcmt) {
        this.textcmt = textcmt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
