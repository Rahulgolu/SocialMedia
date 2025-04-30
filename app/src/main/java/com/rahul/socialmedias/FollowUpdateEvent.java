package com.rahul.socialmedias;

public class FollowUpdateEvent {
    private int followingCount;

    public FollowUpdateEvent(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }
}
