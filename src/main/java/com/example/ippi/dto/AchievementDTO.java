package com.example.ippi.dto;

public class AchievementDTO {
    private Long id;
    private String type;
    private String name;
    private String description;
    private Long threshold;
    private boolean achieved;
    private Long achievedAt;

    public AchievementDTO() {}

    public AchievementDTO(Long id, String type, String name, String description, Long threshold, boolean achieved, Long achievedAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.threshold = threshold;
        this.achieved = achieved;
        this.achievedAt = achievedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }

    public Long getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(Long achievedAt) {
        this.achievedAt = achievedAt;
    }
}
