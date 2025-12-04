package com.example.literatureuniverse.model;

public class AppRule {
    private String ruleId;
    private String title;
    private String description;
    private String actionType; // "comment_review" hoặc "story"
    private long durationMillis; // thời gian cấm

    public AppRule(String ruleId, String title, String description, String actionType, long durationMillis) {
        this.ruleId = ruleId;
        this.title = title;
        this.description = description;
        this.actionType = actionType;
        this.durationMillis = durationMillis;
    }

    public AppRule() {
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }
}
