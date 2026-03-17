package com.example.ippi.dto;

public class ProfileWidgetDTO {
    private String id;
    private String type;
    private String width;
    private String customText;
    private String imageUrl;
    private String linkUrl;

    public ProfileWidgetDTO() {}

    public ProfileWidgetDTO(String id, String type, String width, String customText, String imageUrl, String linkUrl) {
        this.id = id;
        this.type = type;
        this.width = width;
        this.customText = customText;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getCustomText() {
        return customText;
    }

    public void setCustomText(String customText) {
        this.customText = customText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
}