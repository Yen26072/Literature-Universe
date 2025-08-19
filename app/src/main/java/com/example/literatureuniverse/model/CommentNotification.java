package com.example.literatureuniverse.model;

public class CommentNotification {
    private String notificationId;
    private String storyId;
    private String commentId;
    private String replyId;     // null nếu là comment gốc
    private String fromUserId;  // ai gây ra sự kiện
    private String message;     // nội dung hiển thị
    private long createdAt;     // System.currentTimeMillis()
    private boolean isRead;     // true nếu user đã đọc

    public CommentNotification() {
    }

    public CommentNotification(String notificationId, String storyId, String commentId, String replyId, String fromUserId, String message, long createdAt, boolean isRead) {
        this.notificationId = notificationId;
        this.storyId = storyId;
        this.commentId = commentId;
        this.replyId = replyId;
        this.fromUserId = fromUserId;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
