package com.example.literatureuniverse.model;

public class CommentReply {
    private String replyId;
    private String userId;
    private String content;
    private long createdAt;

    private boolean isDeleted;
    private String deletedBy;
    private Long deletedAt;

    private boolean reported;
    private String reportId;

    public CommentReply() {
    }

    public CommentReply(String replyId, String userId, String content, long createdAt, boolean isDeleted, String deletedBy, Long deletedAt, boolean reported, String reportId) {
        this.replyId = replyId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.deletedBy = deletedBy;
        this.deletedAt = deletedAt;
        this.reported = reported;
        this.reportId = reportId;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
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
