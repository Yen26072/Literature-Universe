package com.example.literatureuniverse.model;

public class Appeal {

    private boolean hasAppeal;      // user có khiếu nại không?

    private String appealReason;    // lý do khiếu nại
    private long appealTime;        // thời gian gửi khiếu nại

    private String appealStatus;    // pending | accepted | rejected
    private String appealAdminId;   // admin xử lý khiếu nại
    private long appealDecisionTime;

    public Appeal() {
        // Firebase cần constructor rỗng
    }

    public boolean isHasAppeal() {
        return hasAppeal;
    }

    public void setHasAppeal(boolean hasAppeal) {
        this.hasAppeal = hasAppeal;
    }

    public Appeal(boolean hasAppeal, String appealReason, long appealTime, String appealStatus, String appealAdminId, long appealDecisionTime) {
        this.hasAppeal = hasAppeal;
        this.appealReason = appealReason;
        this.appealTime = appealTime;
        this.appealStatus = appealStatus;
        this.appealAdminId = appealAdminId;
        this.appealDecisionTime = appealDecisionTime;
    }

    public String getAppealReason() {
        return appealReason;
    }

    public void setAppealReason(String appealReason) {
        this.appealReason = appealReason;
    }

    public long getAppealTime() {
        return appealTime;
    }

    public void setAppealTime(long appealTime) {
        this.appealTime = appealTime;
    }

    public String getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(String appealStatus) {
        this.appealStatus = appealStatus;
    }

    public String getAppealAdminId() {
        return appealAdminId;
    }

    public void setAppealAdminId(String appealAdminId) {
        this.appealAdminId = appealAdminId;
    }

    public long getAppealDecisionTime() {
        return appealDecisionTime;
    }

    public void setAppealDecisionTime(long appealDecisionTime) {
        this.appealDecisionTime = appealDecisionTime;
    }


}

