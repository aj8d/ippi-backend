package com.example.ippi.dto;

/**
 * ユーザー検索結果のDTO
 * パスワードなどの機密情報を除外
 */
public class UserSearchResult {
    private Long id;
    private String name;
    private String customId;
    private String profileImageUrl;
    private String description;

    public UserSearchResult() {}

    public UserSearchResult(Long id, String name, String customId, String profileImageUrl, String description) {
        this.id = id;
        this.name = name;
        this.customId = customId;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
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
}
