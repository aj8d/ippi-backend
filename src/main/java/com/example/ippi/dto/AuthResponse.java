package com.example.ippi.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String name;
    private String profileImageUrl;
    private String description;
    private String customId;
    private String profileThemePreset;
    private Object profileTheme;
    private String profileBackgroundUrl;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String email, String name) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public AuthResponse(String token, Long userId, String email, String name, String profileImageUrl) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public AuthResponse(String token, Long userId, String email, String name, String profileImageUrl, String description) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
    }

    public AuthResponse(String token, Long userId, String email, String name, String profileImageUrl, String description, String customId) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
        this.customId = customId;
    }

    public AuthResponse(
            String token,
            Long userId,
            String email,
            String name,
            String profileImageUrl,
            String description,
            String customId,
            String profileThemePreset
    ) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.description = description;
        this.customId = customId;
        this.profileThemePreset = profileThemePreset;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public String getProfileThemePreset() {
        return profileThemePreset;
    }

    public void setProfileThemePreset(String profileThemePreset) {
        this.profileThemePreset = profileThemePreset;
    }

    public Object getProfileTheme() {
        return profileTheme;
    }

    public void setProfileTheme(Object profileTheme) {
        this.profileTheme = profileTheme;
    }

    public String getProfileBackgroundUrl() {
        return profileBackgroundUrl;
    }

    public void setProfileBackgroundUrl(String profileBackgroundUrl) {
        this.profileBackgroundUrl = profileBackgroundUrl;
    }
}
