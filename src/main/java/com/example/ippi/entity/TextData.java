package com.example.ippi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "text_data")
public class TextData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "timer_seconds", nullable = false)
    private Long timerSeconds = 0L;

    @Column(name = "timer_started_at")
    private Long timerStartedAt;

    @Column(name = "timer_running", nullable = false)
    private Boolean timerRunning = false;

    public TextData() {}

    public TextData(Long userId, String text, Long createdAt, Long updatedAt) {
        this.userId = userId;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.timerSeconds = 0L;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getTimerSeconds() {
        return timerSeconds;
    }

    public void setTimerSeconds(Long timerSeconds) {
        this.timerSeconds = timerSeconds;
    }

    public Long getTimerStartedAt() {
        return timerStartedAt;
    }

    public void setTimerStartedAt(Long timerStartedAt) {
        this.timerStartedAt = timerStartedAt;
    }

    public Boolean getTimerRunning() {
        return timerRunning;
    }

    public void setTimerRunning(Boolean timerRunning) {
        this.timerRunning = timerRunning;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}
