package com.example.ippi.entity;

import jakarta.persistence.*;

/**
 * ユーザー統計エンティティ
 * 
 * 📚 このエンティティの役割：
 * - ユーザーごとの累計統計データを保存
 * - フィードやプロフィールウィジェットで表示
 */
@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 連続作業日数（現在のストリーク）
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    // 最長連続作業日数（過去最高記録）
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0;

    // 累計作業日数
    @Column(name = "total_work_days", nullable = false)
    private Integer totalWorkDays = 0;

    // 完了したTodo数
    @Column(name = "completed_todos", nullable = false)
    private Integer completedTodos = 0;

    // 累計作業時間（秒）
    @Column(name = "total_work_seconds", nullable = false)
    private Long totalWorkSeconds = 0L;

    // 今週の作業時間（秒）
    @Column(name = "weekly_work_seconds", nullable = false)
    private Long weeklyWorkSeconds = 0L;

    // 今週の開始日（リセット判定用）
    @Column(name = "weekly_start_date")
    private String weeklyStartDate;

    // 今月の作業時間（秒）
    @Column(name = "monthly_work_seconds", nullable = false)
    private Long monthlyWorkSeconds = 0L;

    // 今月の開始日（リセット判定用）
    @Column(name = "monthly_start_date")
    private String monthlyStartDate;

    // 最後に作業した日付（ストリーク計算用）
    @Column(name = "last_work_date")
    private String lastWorkDate;

    // 総タイマー使用回数
    @Column(name = "total_timer_sessions", nullable = false)
    private Integer totalTimerSessions = 0;

    // 今日のタイマー完了回数（作業セッション完了）
    @Column(name = "daily_timer_completions", nullable = false)
    private Integer dailyTimerCompletions = 0;

    // 最後にタイマーを完了した日付（日付リセット用）
    @Column(name = "last_completion_date")
    private String lastCompletionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    public UserStats() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public UserStats(User user) {
        this.user = user;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.totalWorkDays = 0;
        this.completedTodos = 0;
        this.totalWorkSeconds = 0L;
        this.weeklyWorkSeconds = 0L;
        this.monthlyWorkSeconds = 0L;
        this.totalTimerSessions = 0;
        this.dailyTimerCompletions = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
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

    public String getWeeklyStartDate() {
        return weeklyStartDate;
    }

    public void setWeeklyStartDate(String weeklyStartDate) {
        this.weeklyStartDate = weeklyStartDate;
    }

    public Long getMonthlyWorkSeconds() {
        return monthlyWorkSeconds;
    }

    public void setMonthlyWorkSeconds(Long monthlyWorkSeconds) {
        this.monthlyWorkSeconds = monthlyWorkSeconds;
    }

    public String getMonthlyStartDate() {
        return monthlyStartDate;
    }

    public void setMonthlyStartDate(String monthlyStartDate) {
        this.monthlyStartDate = monthlyStartDate;
    }

    public String getLastWorkDate() {
        return lastWorkDate;
    }

    public void setLastWorkDate(String lastWorkDate) {
        this.lastWorkDate = lastWorkDate;
    }

    public Integer getTotalTimerSessions() {
        return totalTimerSessions;
    }

    public void setTotalTimerSessions(Integer totalTimerSessions) {
        this.totalTimerSessions = totalTimerSessions;
    }

    public Integer getDailyTimerCompletions() {
        return dailyTimerCompletions;
    }

    public void setDailyTimerCompletions(Integer dailyTimerCompletions) {
        this.dailyTimerCompletions = dailyTimerCompletions;
    }

    public String getLastCompletionDate() {
        return lastCompletionDate;
    }

    public void setLastCompletionDate(String lastCompletionDate) {
        this.lastCompletionDate = lastCompletionDate;
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
