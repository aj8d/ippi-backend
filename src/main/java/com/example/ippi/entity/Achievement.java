package com.example.ippi.entity;

import jakarta.persistence.*;

/**
 * アチーブメント定義エンティティ
 */
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // アチーブメントの種類（work_time, streak, timer_count, todo_count）
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    // アチーブメント名
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 説明
    @Column(name = "description", length = 500)
    private String description;

    // 達成条件の閾値（時間は秒単位、回数はそのまま、連続日数は日数）
    @Column(name = "threshold", nullable = false)
    private Long threshold;

    // 表示順序
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    // アチーブメントタイプの定数
    public static final String TYPE_WORK_TIME = "work_time";      // 累計作業時間
    public static final String TYPE_STREAK = "streak";            // 連続作業日数
    public static final String TYPE_TIMER_COUNT = "timer_count";  // タイマー使用回数
    public static final String TYPE_TODO_COUNT = "todo_count";    // Todo完了数

    public Achievement() {
        this.createdAt = System.currentTimeMillis();
    }

    public Achievement(String type, String name, String description, Long threshold, Integer displayOrder) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.threshold = threshold;
        this.displayOrder = displayOrder;
        this.createdAt = System.currentTimeMillis();
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
