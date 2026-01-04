package com.example.ippi.dto;

/**
 * 📚 WidgetDTO
 * 
 * フロントエンドとのデータ交換用オブジェクト
 * Entity と同じ構造だが、User オブジェクトの代わりに必要な情報のみ含む
 */
public class WidgetDTO {
    private String id;      // widgetId (フロントエンド用)
    private String type;
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private Object data;    // JSON パース後のオブジェクト

    // デフォルトコンストラクタ
    public WidgetDTO() {}

    public WidgetDTO(String id, String type, Double x, Double y, 
                     Double width, Double height, Object data) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
