package com.example.literatureuniverse.model;

public class StoryReport {
    private String reportId;
    private String storyId;
    private String chapterId;

    private String reporterId;  //người gửi báo caos
    private String storyOwnerId;   //người bị báo cáo

    private String reason;  //lý do báo cáo, mô tả chi tiết
    private long timestamp;

    private String status;  // chờ xử lý //pending / accepted / rejected
    private String adminId;     //Admin nào xử lý
    private long adminDecisionTime; //Thời gian admin ra quyết định
    private String adminNote;   //Admin ghi chú: “ngôn từ xúc phạm rõ ràng…”

    private String punishment;  //Mức phạt người comment (nếu accepted)
    private String violationId;     //ID vi phạm trong userViolations liên kết

    private Appeal appeal;

    public StoryReport() {
    }

    public StoryReport(String reportId, String storyId, String chapterId, String reporterId, String storyOwnerId, String reason, long timestamp, String status, String adminId, long adminDecisionTime, String adminNote, String punishment, String violationId, Appeal appeal) {
        this.reportId = reportId;
        this.storyId = storyId;
        this.chapterId = chapterId;
        this.reporterId = reporterId;
        this.storyOwnerId = storyOwnerId;
        this.reason = reason;
        this.timestamp = timestamp;
        this.status = status;
        this.adminId = adminId;
        this.adminDecisionTime = adminDecisionTime;
        this.adminNote = adminNote;
        this.punishment = punishment;
        this.violationId = violationId;
        this.appeal = appeal;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
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

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getStoryOwnerId() {
        return storyOwnerId;
    }

    public void setStoryOwnerId(String storyOwnerId) {
        this.storyOwnerId = storyOwnerId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public long getAdminDecisionTime() {
        return adminDecisionTime;
    }

    public void setAdminDecisionTime(long adminDecisionTime) {
        this.adminDecisionTime = adminDecisionTime;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public String getPunishment() {
        return punishment;
    }

    public void setPunishment(String punishment) {
        this.punishment = punishment;
    }

    public String getViolationId() {
        return violationId;
    }

    public void setViolationId(String violationId) {
        this.violationId = violationId;
    }

    public Appeal getAppeal() {
        return appeal;
    }

    public void setAppeal(Appeal appeal) {
        this.appeal = appeal;
    }
}
