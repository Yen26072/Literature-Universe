package com.example.literatureuniverse.model;

public class FollowStory {
    private String storyId;
    private String userId;
    private long followedAt; // thời gian follow, để sắp xếp

    public FollowStory() {
    }

    public FollowStory(String storyId, String userId, long followedAt) {
        this.storyId = storyId;
        this.userId = userId;
        this.followedAt = followedAt;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(long followedAt) {
        this.followedAt = followedAt;
    }
}
