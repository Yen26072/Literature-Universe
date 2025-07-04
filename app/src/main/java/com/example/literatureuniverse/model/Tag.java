package com.example.literatureuniverse.model;

public class Tag {
    private String id;
    private String label;
    private int priority;
    private String unsplashKeyword;

    public Tag() {
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
