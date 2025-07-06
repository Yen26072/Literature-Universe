package com.example.literatureuniverse.model;

import java.io.Serializable;

public class Tag implements Serializable {
    private String id;
    private String label;
    private int priority;
    private String unsplashKeyword;
    private boolean isChecked;

    public Tag() {
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public Tag(String id, String label, int priority, String unsplashKeyword, boolean isChecked) {
        this.id = id;
        this.label = label;
        this.priority = priority;
        this.unsplashKeyword = unsplashKeyword;
        this.isChecked = isChecked;
    }

    public Tag(String id, String label, int priority, String unsplashKeyword) {
        this.id = id;
        this.label = label;
        this.priority = priority;
        this.unsplashKeyword = unsplashKeyword;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getUnsplashKeyword() {
        return unsplashKeyword;
    }

    public void setUnsplashKeyword(String unsplashKeyword) {
        this.unsplashKeyword = unsplashKeyword;
    }
}
