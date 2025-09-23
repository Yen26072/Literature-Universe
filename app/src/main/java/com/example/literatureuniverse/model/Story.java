package com.example.literatureuniverse.model;

import java.io.Serializable;
import java.util.List;

public class Story implements Serializable {
    private String storyId;
    private String title;
    private String description;
    private String authorId;
    private String coverUrl;
    private List<String> tags;
    private String status; // "đang tiếp", "hoàn thành", "tạm ngưng"
    private long createdAt;
    private long updatedAt;
    private int likesCount;
    private int viewsCount;
    private int commentsCount;
    private int followersCount;
    private int chaptersCount;
    private boolean isDeleted;
    private String modifiedBy;
    private long modifiedAt;
    private LatestChapter latestChapter;
    // Trường tạm để sort, không lưu vào Firebase
    private transient long sortTime;

    public int getChaptersCount() {
        return chaptersCount;
    }

    public void setChaptersCount(int chaptersCount) {
        this.chaptersCount = chaptersCount;
    }

    public long getSortTime() {
        return sortTime;
    }

    public void setSortTime(long sortTime) {
        this.sortTime = sortTime;
    }

    public String getStoryId() {
        return storyId;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public Story(String storyId, String title, String description, String authorId, String coverUrl, List<String> tags, String status, long createdAt, long updatedAt, int likesCount, int viewsCount, int commentsCount, int followersCount, int chaptersCount, boolean isDeleted, String modifiedBy, long modifiedAt, LatestChapter latestChapter) {
        this.storyId = storyId;
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likesCount = likesCount;
        this.viewsCount = viewsCount;
        this.commentsCount = commentsCount;
        this.followersCount = followersCount;
        this.chaptersCount = chaptersCount;
        this.isDeleted = isDeleted;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
        this.latestChapter = latestChapter;
    }

    public Story(String storyId, String title, String description, String authorId, String coverUrl, List<String> tags, String status, long createdAt, long updatedAt, int likesCount, int viewsCount, int commentsCount, int followersCount, boolean isDeleted, String modifiedBy, long modifiedAt, LatestChapter latestChapter) {
        this.storyId = storyId;
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likesCount = likesCount;
        this.viewsCount = viewsCount;
        this.commentsCount = commentsCount;
        this.followersCount = followersCount;
        this.isDeleted = isDeleted;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
        this.latestChapter = latestChapter;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public Story(String storyId, String title, String description, String authorId, String coverUrl, List<String> tags, String status, long createdAt, long updatedAt, int likesCount, int viewsCount, int followersCount, boolean isDeleted, String modifiedBy, long modifiedAt, LatestChapter latestChapter) {
        this.storyId = storyId;
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likesCount = likesCount;
        this.viewsCount = viewsCount;
        this.followersCount = followersCount;
        this.isDeleted = isDeleted;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
        this.latestChapter = latestChapter;
    }

    public Story() {
    }

    public Story(String status, String title, String description, String authorId, String coverUrl, List<String> tags, long createdAt, long updatedAt, int likesCount, int viewsCount, int followersCount, boolean isDeleted, String modifiedBy, long modifiedAt, LatestChapter latestChapter) {
        this.status = status;
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likesCount = likesCount;
        this.viewsCount = viewsCount;
        this.followersCount = followersCount;
        this.isDeleted = isDeleted;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = modifiedAt;
        this.latestChapter = latestChapter;
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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public LatestChapter getLatestChapter() {
        return latestChapter;
    }

    public void setLatestChapter(LatestChapter latestChapter) {
        this.latestChapter = latestChapter;
    }

    public static class LatestChapter {
        private String chapterId;
        private String title;
        private long createdAt;

        public LatestChapter() {
        }

        public LatestChapter(String chapterId, String title, long createdAt) {
            this.chapterId = chapterId;
            this.title = title;
            this.createdAt = createdAt;
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

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
    }
}
