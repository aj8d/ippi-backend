package com.example.ippi.entity;

import jakarta.persistence.*;

/**
 * キャンバス上のウィジェット情報をデータベースに保存
 * - 位置 (x, y)
 * - サイズ (width, height)
 * - タイプ (timer, todo, sticky, image など)
 * - カスタムデータ (JSON形式で保存)
 */
@Entity
@Table(name = "widgets")
public class Widget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ウィジェットの所有者
     * 多対一の関係: 1人のユーザーが複数のウィジェットを持てる
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * フロントエンドで使うウィジェットID
     * "widget-1234567890" のような形式
     */
    @Column(name = "widget_id", nullable = false)
    private String widgetId;

    // ウィジェットのタイプ
    @Column(nullable = false, length = 50)
    private String type;

    // 位置とサイズ
    @Column(nullable = false)
    private Double x;
    @Column(nullable = false)
    private Double y;
    @Column(nullable = false)
    private Double width;
    @Column(nullable = false)
    private Double height;

    /**
     * ウィジェット固有のデータ（JSON形式）
     */
    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    // コンストラクタ
    public Widget() {}

    public Widget(User user, String widgetId, String type, Double x, Double y, 
                  Double width, Double height, String data) {
        this.user = user;
        this.widgetId = widgetId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.data = data;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getWidgetId() { return widgetId; }
    public void setWidgetId(String widgetId) { this.widgetId = widgetId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public Double getWidth() { return width; }
    public void setWidth(Double width) { this.width = width; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
