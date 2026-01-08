package com.example.ippi.dto;

public class DailyStats {
    private String date;      // YYYY-MM-DD 形式
    private Integer count;

    public DailyStats() {}

    public DailyStats(String date, Integer count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
