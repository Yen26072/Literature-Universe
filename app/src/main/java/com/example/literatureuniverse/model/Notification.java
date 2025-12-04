package com.example.literatureuniverse.model;

public class Notification {
    private String notificationId;
    private String reportId;        // báo cáo nào?
    private String reporterId;
    private String reportType;      // comment | review | story
    private String storyId;         // báo cáo thuộc truyện nào?
    private String chapterId;       // nếu report thuộc chương thì lưu
    private String commentId;       // nếu là report comment
    private String reviewId;        // nếu là report review
    private long timestamp;
    private boolean read;
    private String message;         // nội dung thông báo

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public Notification(String notificationId, String reportId, String reporterId, String reportType, String storyId, String chapterId, String commentId, String reviewId, long timestamp, boolean read, String message) {
        this.notificationId = notificationId;
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.reportType = reportType;
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.commentId = commentId;
        this.reviewId = reviewId;
        this.timestamp = timestamp;
        this.read = read;
        this.message = message;
    }

    public Notification() {
    }

    public Notification(String notificationId, String reportId, String reportType, String storyId, String chapterId, String commentId, String reviewId, long timestamp, boolean read, String message) {
        this.notificationId = notificationId;
        this.reportId = reportId;
        this.reportType = reportType;
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.commentId = commentId;
        this.reviewId = reviewId;
        this.timestamp = timestamp;
        this.read = read;
        this.message = message;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
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

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
