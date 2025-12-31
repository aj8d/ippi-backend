package com.example.ippi.dto;

public class FollowUserDTO {
    private Long id;
    private String name;
    private String customId;
    private String profileImageUrl;
    private String description;
    private boolean isFollowing; // 自分がこのユーザーをフォローしているか

    public FollowUserDTO() {}

    public FollowUserDTO(Long id, String name, String customId, String profileImageUrl, String description, boolean isFollowing) {
        this.id = id;
        this.name = name;
        this.customId = customId;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
        this.isFollowing = isFollowing;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}
