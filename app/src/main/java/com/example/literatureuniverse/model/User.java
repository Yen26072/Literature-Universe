package com.example.literatureuniverse.model;

public class User {
    private String userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private boolean isMuted;
    private Long muteUntil;
    private  boolean canPost;
    private Long postBanUntil;
    private int violationCount;

    public boolean isMuted() {
        return isMuted;
    }

    private long createdAt;
    private String bio;
    private String coverUrl;

    public User() {
    }

    public Long getMuteUntil() {
        return muteUntil;
    }

    public void setMuteUntil(Long muteUntil) {
        this.muteUntil = muteUntil;
    }

    public Long getPostBanUntil() {
        return postBanUntil;
    }

    public void setPostBanUntil(Long postBanUntil) {
        this.postBanUntil = postBanUntil;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public User(String userId, String username, String email, String avatarUrl, String role, boolean isMuted, Long muteUntil, boolean canPost, Long postBanUntil, int violationCount, long createdAt, String bio, String coverUrl) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.isMuted = isMuted;
        this.muteUntil = muteUntil;
        this.canPost = canPost;
        this.postBanUntil = postBanUntil;
        this.violationCount = violationCount;
        this.createdAt = createdAt;
        this.bio = bio;
        this.coverUrl = coverUrl;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isMuted(boolean b) {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public boolean isCanPost() {
        return canPost;
    }

    public void setCanPost(boolean canPost) {
        this.canPost = canPost;
    }

    public int getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(int violationCount) {
        this.violationCount = violationCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
