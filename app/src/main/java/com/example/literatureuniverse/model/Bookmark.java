package com.example.literatureuniverse.model;

public class Bookmark {
    private String storyId;
    private String chapterId;
    private long timestamp;

    public Bookmark() {
    }

    public Bookmark(String storyId, String chapterId, long timestamp) {
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.timestamp = timestamp;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
