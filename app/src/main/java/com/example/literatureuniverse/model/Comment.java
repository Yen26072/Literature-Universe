package com.example.literatureuniverse.model;

public class Comment {
    private String commentId;
    private String userId;
    private String storyId;
    private String chapterId; // null nếu là comment review
    private String reviewId;  // null nếu không phải comment trong review

    private String content;
    private long createdAt;

    private boolean isDeleted;
    private String deletedBy;
    private Long deletedAt;  // dùng Long vì có thể null

    private int replyCount;

    private boolean reported;
    private String reportId;

    public Comment(String commentId, String userId, String storyId, String chapterId, String reviewId, String content, long createdAt, boolean isDeleted, String deletedBy, Long deletedAt, int replyCount, boolean reported, String reportId) {
        this.commentId = commentId;
        this.userId = userId;
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.reviewId = reviewId;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        this.replyCount = replyCount;
        this.reported = reported;
        this.reportId = reportId;
    }

    public Comment() {
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
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

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
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
