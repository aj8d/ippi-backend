package com.example.ippi.dto;

import java.util.List;

public class FeedItemDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userCustomId;
    private String userProfileImageUrl;
    private String activityType;
    private String message;
    private String relatedData;
    private Long createdAt;
    private long likeCount;
    private boolean isLiked;
    private long commentCount;
    private List<CommentDTO> comments;

    public FeedItemDTO() {}

    public FeedItemDTO(Long id, Long userId, String userName, String userCustomId, 
                       String userProfileImageUrl, String activityType, String message, 
                       String relatedData, Long createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userCustomId = userCustomId;
        this.userProfileImageUrl = userProfileImageUrl;
        this.activityType = activityType;
        this.message = message;
        this.relatedData = relatedData;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCustomId() {
        return userCustomId;
    }

    public void setUserCustomId(String userCustomId) {
        this.userCustomId = userCustomId;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedData() {
        return relatedData;
    }

    public void setRelatedData(String relatedData) {
        this.relatedData = relatedData;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }
}
