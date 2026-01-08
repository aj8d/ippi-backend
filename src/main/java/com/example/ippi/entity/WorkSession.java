package com.example.ippi.entity;

import jakarta.persistence.*;

/**
 * WorkSession - 作業セッション記録エンティティ
 * 
 * TODOとは独立して作業時間を記録するためのテーブル
 */
@Entity
@Table(name = "work_sessions")
public class WorkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String workDate; // YYYY-MM-DD形式

    @Column(nullable = false)
    private Long timerSeconds; // 作業時間（秒単位）

    @Column(nullable = false)
    private Long createdAt;

    @Column(nullable = false)
    private Long updatedAt;

    // コンストラクタ
    public WorkSession() {}

    public WorkSession(Long userId, String workDate, Long timerSeconds) {
        this.userId = userId;
        this.workDate = workDate;
        this.timerSeconds = timerSeconds;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getter/Setter
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

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public Long getTimerSeconds() {
        return timerSeconds;
    }

    public void setTimerSeconds(Long timerSeconds) {
        this.timerSeconds = timerSeconds;
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
}
