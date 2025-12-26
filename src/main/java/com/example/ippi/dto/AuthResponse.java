package com.example.ippi.dto;

public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String name;
    private String profileImageUrl;
    private String description;

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
}
