package com.rahul.socialmedias.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Posts {

    private String postId;
    private String userId;
    private String content;
    private String imageUrl;
    private long createdAt;
    private long updatedAt;
    private boolean isPrivate;
    private List<String> likes = new ArrayList<>();
    private ArrayList<Comments> comments;
    private ArrayList<String> dislikes;
    private Map<String, Long> snoozedBy = new HashMap<>();
    private Map<String, Boolean> hiddenBy = new HashMap<>();

    public Posts() {
    }

    public ArrayList<Comments> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comments> comments) {
        this.comments = comments;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public ArrayList<String> getDislikes() {
        return dislikes;
    }

    public void setDislikes(ArrayList<String> dislikes) {
        this.dislikes = dislikes;
    }

    public Map<String, Long> getSnoozedBy() {
        return snoozedBy;
    }

    public void setSnoozedBy(Map<String, Long> snoozedBy) {
        this.snoozedBy = snoozedBy;
    }

    public Map<String, Boolean> getHiddenBy() {
        return hiddenBy;
    }

    public void setHiddenBy(Map<String, Boolean> hiddenBy) {
        this.hiddenBy = hiddenBy;
    }
}
