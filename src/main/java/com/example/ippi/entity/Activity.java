package com.example.ippi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "related_data", length = 1000)
    private String relatedData; // JSON形式で追加データを保存

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    public Activity() {}

    public Activity(User user, String activityType, String message, String relatedData, Long createdAt) {
        this.user = user;
        this.activityType = activityType;
        this.message = message;
        this.relatedData = relatedData;
        this.createdAt = createdAt;
    }

    // アクティビティタイプの定数
    public static final String TYPE_WORK_COMPLETED = "work_completed";
    public static final String TYPE_FOLLOW = "follow";
    public static final String TYPE_ACHIEVEMENT = "achievement";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
