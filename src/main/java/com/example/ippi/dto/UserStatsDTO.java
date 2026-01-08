package com.example.ippi.dto;

/**
 * ユーザー統計DTOクラス
 * APIレスポンス用
 */
public class UserStatsDTO {
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer totalWorkDays;
    private Integer completedTodos;
    private Long totalWorkSeconds;
    private Long weeklyWorkSeconds;
    private Long monthlyWorkSeconds;
    private Integer totalTimerSessions;
    private Integer dailyTimerCompletions;
    
    // 計算値（時間単位）
    private Double totalWorkHours;
    private Double weeklyWorkHours;
    private Double monthlyWorkHours;
    private Double averageWorkMinutesPerDay;

    public UserStatsDTO() {}

    public UserStatsDTO(Integer currentStreak, Integer longestStreak, Integer totalWorkDays,
                        Integer completedTodos, Long totalWorkSeconds, Long weeklyWorkSeconds,
                        Long monthlyWorkSeconds, Integer totalTimerSessions, Integer dailyTimerCompletions) {
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.totalWorkDays = totalWorkDays;
        this.completedTodos = completedTodos;
        this.totalWorkSeconds = totalWorkSeconds;
        this.weeklyWorkSeconds = weeklyWorkSeconds;
        this.monthlyWorkSeconds = monthlyWorkSeconds;
        this.totalTimerSessions = totalTimerSessions;
        this.dailyTimerCompletions = dailyTimerCompletions;
        
        // 時間単位に変換
        this.totalWorkHours = totalWorkSeconds / 3600.0;
        this.weeklyWorkHours = weeklyWorkSeconds / 3600.0;
        this.monthlyWorkHours = monthlyWorkSeconds / 3600.0;
        
        // 平均作業時間（分/日）
        if (totalWorkDays > 0) {
            this.averageWorkMinutesPerDay = (totalWorkSeconds / 60.0) / totalWorkDays;
        } else {
            this.averageWorkMinutesPerDay = 0.0;
        }
    }

    // Getters and Setters
    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public Integer getTotalWorkDays() {
        return totalWorkDays;
    }

    public void setTotalWorkDays(Integer totalWorkDays) {
        this.totalWorkDays = totalWorkDays;
    }

    public Integer getCompletedTodos() {
        return completedTodos;
    }

    public void setCompletedTodos(Integer completedTodos) {
        this.completedTodos = completedTodos;
    }

    public Long getTotalWorkSeconds() {
        return totalWorkSeconds;
    }

    public void setTotalWorkSeconds(Long totalWorkSeconds) {
        this.totalWorkSeconds = totalWorkSeconds;
    }

    public Long getWeeklyWorkSeconds() {
        return weeklyWorkSeconds;
    }

    public void setWeeklyWorkSeconds(Long weeklyWorkSeconds) {
        this.weeklyWorkSeconds = weeklyWorkSeconds;
    }

    public Long getMonthlyWorkSeconds() {
        return monthlyWorkSeconds;
    }

    public void setMonthlyWorkSeconds(Long monthlyWorkSeconds) {
        this.monthlyWorkSeconds = monthlyWorkSeconds;
    }

    public Integer getTotalTimerSessions() {
        return totalTimerSessions;
    }

    public void setTotalTimerSessions(Integer totalTimerSessions) {
        this.totalTimerSessions = totalTimerSessions;
    }

    public Double getTotalWorkHours() {
        return totalWorkHours;
    }

    public void setTotalWorkHours(Double totalWorkHours) {
        this.totalWorkHours = totalWorkHours;
    }

    public Double getWeeklyWorkHours() {
        return weeklyWorkHours;
    }

    public void setWeeklyWorkHours(Double weeklyWorkHours) {
        this.weeklyWorkHours = weeklyWorkHours;
    }

    public Double getMonthlyWorkHours() {
        return monthlyWorkHours;
    }

    public void setMonthlyWorkHours(Double monthlyWorkHours) {
        this.monthlyWorkHours = monthlyWorkHours;
    }

    public Double getAverageWorkMinutesPerDay() {
        return averageWorkMinutesPerDay;
    }

    public void setAverageWorkMinutesPerDay(Double averageWorkMinutesPerDay) {
        this.averageWorkMinutesPerDay = averageWorkMinutesPerDay;
    }

    public Integer getDailyTimerCompletions() {
        return dailyTimerCompletions;
    }

    public void setDailyTimerCompletions(Integer dailyTimerCompletions) {
        this.dailyTimerCompletions = dailyTimerCompletions;
    }
}
