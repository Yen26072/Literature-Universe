package com.example.literatureuniverse.model;

import java.io.Serializable;

public class Chapter implements Serializable {
    private String chapterId;
    private String title;
    private String content;
    private long createdAt;

    private Long updatedAt;
    private boolean isDeleted;
    private Long deletedAt;
    private String deletedBy;
    private String storyId; // 👈 Thêm dòng này nếu bạn cần lưu ID truyện

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public Chapter(String chapterId, String title, String content, long createdAt, Long updatedAt, boolean isDeleted, Long deletedAt, String deletedBy, String storyId) {
        this.chapterId = chapterId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.storyId = storyId;
    }

    public Chapter() {
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Chapter(String chapterId, String title, String content, long createdAt, Long updatedAt, boolean isDeleted, Long deletedAt, String deletedBy) {
        this.chapterId = chapterId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }
}
