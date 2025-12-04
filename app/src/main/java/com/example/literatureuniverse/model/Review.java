package com.example.literatureuniverse.model;

public class Review {
    private String reviewId;
    private String userId;
    private String storyId;

    private String content;
    private long createdAt;

    private boolean isDeleted;
    private String deletedBy;
    private Long deletedAt;  // dùng Long vì có thể null

    private int replyCount;

    private boolean reported;
    private String reportId;

    public Review() {
    }

    public Review(String reviewId, String userId, String storyId, String content, long createdAt, boolean isDeleted, String deletedBy, Long deletedAt, int replyCount, boolean reported, String reportId) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.storyId = storyId;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        this.replyCount = replyCount;
        this.reported = reported;
        this.reportId = reportId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public boolean isReported() {
        return reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
