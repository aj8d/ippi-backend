package com.example.ippi.dto;

import java.util.List;

public class StatsResponse {
    private List<DailyStats> stats;

    public StatsResponse() {}

    public StatsResponse(List<DailyStats> stats) {
        this.stats = stats;
    }

    public List<DailyStats> getStats() {
        return stats;
    }

    public void setStats(List<DailyStats> stats) {
        this.stats = stats;
    }
}
