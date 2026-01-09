package com.example.ippi.service;

import com.example.ippi.dto.DailyStats;
import com.example.ippi.dto.UserStatsDTO;
import com.example.ippi.entity.User;
import com.example.ippi.entity.UserStats;
import com.example.ippi.entity.WorkSession;
import com.example.ippi.repository.UserStatsRepository;
import com.example.ippi.repository.WorkSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ユーザー統計サービス
 * 
 * - ユーザー統計の取得・更新
 * - ストリーク計算
 * - 週次・月次リセット
 */
@Service
public class UserStatsService {

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private WorkSessionRepository workSessionRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * ユーザーの統計を取得（なければ作成）
     */
    public UserStats getOrCreateStats(User user) {
        return userStatsRepository.findByUser(user)
                .orElseGet(() -> {
                    UserStats stats = new UserStats(user);
                    return userStatsRepository.save(stats);
                });
    }

    /**
     * ユーザーIDで統計を取得
     */
    public UserStatsDTO getStatsByUserId(Long userId) {
        UserStats stats = userStatsRepository.findByUserId(userId).orElse(null);
        
        if (stats == null) {
            // 統計がない場合はデフォルト値を返す
            return new UserStatsDTO(0, 0, 0, 0, 0L, 0L, 0L, 0, 0);
        }

        // 週次・月次リセットをチェック
        checkAndResetPeriods(stats);

        // 日次タイマー完了数のリセットをチェック
        int dailyCompletions = getDailyTimerCompletions(stats);

        return new UserStatsDTO(
            stats.getCurrentStreak(),
            stats.getLongestStreak(),
            stats.getTotalWorkDays(),
            stats.getCompletedTodos(),
            stats.getTotalWorkSeconds(),
            stats.getWeeklyWorkSeconds(),
            stats.getMonthlyWorkSeconds(),
            stats.getTotalTimerSessions(),
            dailyCompletions
        );
    }

    /**
     * 日次タイマー完了数を取得（日付が変わっていれば0を返す）
     */
    private int getDailyTimerCompletions(UserStats stats) {
        if (stats.getDailyTimerCompletions() == null || stats.getDailyTimerCompletions() == 0) {
            return 0;
        }

        // 最後の完了日をチェック
        String lastCompletionDate = stats.getLastCompletionDate();
        if (lastCompletionDate == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMAT);

        // 日付が変わっていれば0を返す
        if (!lastCompletionDate.equals(todayStr)) {
            return 0;
        }

        return stats.getDailyTimerCompletions();
    }

    /**
     * 作業セッション完了時に統計を更新
     */
    @Transactional
    public void recordWorkSession(User user, String workDate, long seconds) {
        UserStats stats = getOrCreateStats(user);
        LocalDate today = LocalDate.parse(workDate, DATE_FORMAT);
        
        // 週次・月次リセットをチェック
        checkAndResetPeriods(stats);

        // 更新前のストリーク
        int previousStreak = stats.getCurrentStreak();

        // ストリーク更新
        updateStreak(stats, today);

        // 累計作業時間を更新
        stats.setTotalWorkSeconds(stats.getTotalWorkSeconds() + seconds);
        stats.setWeeklyWorkSeconds(stats.getWeeklyWorkSeconds() + seconds);
        stats.setMonthlyWorkSeconds(stats.getMonthlyWorkSeconds() + seconds);

        // タイマーセッション数を増加
        stats.setTotalTimerSessions(stats.getTotalTimerSessions() + 1);

        // 更新日時
        stats.setUpdatedAt(System.currentTimeMillis());

        userStatsRepository.save(stats);

        // アチーブメント判定を実行
        achievementService.checkAndAwardAchievements(user, stats);
    }

    /**
     * Todo完了時に統計を更新
     */
    @Transactional
    public void recordTodoCompleted(User user) {
        UserStats stats = getOrCreateStats(user);
        stats.setCompletedTodos(stats.getCompletedTodos() + 1);
        stats.setUpdatedAt(System.currentTimeMillis());
        userStatsRepository.save(stats);
        
        // アチーブメント判定を実行
        achievementService.checkAndAwardAchievements(user, stats);
    }

    /**
     * Todo未完了に戻した時に統計を更新
     */
    @Transactional
    public void recordTodoUncompleted(User user) {
        UserStats stats = getOrCreateStats(user);
        if (stats.getCompletedTodos() > 0) {
            stats.setCompletedTodos(stats.getCompletedTodos() - 1);
            stats.setUpdatedAt(System.currentTimeMillis());
            userStatsRepository.save(stats);
        }
    }

    /**
     * タイマー完了時に今日のカウントを加算
     * ポモドーロ/フローモドーロの作業セッション完了時にのみ呼び出される
     */
    @Transactional
    public void recordTimerCompletion(User user) {
        UserStats stats = getOrCreateStats(user);
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMAT);

        // 日付が変わっていればリセット
        if (stats.getLastCompletionDate() == null || !stats.getLastCompletionDate().equals(todayStr)) {
            stats.setDailyTimerCompletions(1);
            stats.setLastCompletionDate(todayStr);
        } else {
            // 同じ日ならカウント加算
            stats.setDailyTimerCompletions(stats.getDailyTimerCompletions() + 1);
        }

        stats.setUpdatedAt(System.currentTimeMillis());
        userStatsRepository.save(stats);
    }

    /**
     * ストリークを更新
     */
    private void updateStreak(UserStats stats, LocalDate workDate) {
        String lastWorkDateStr = stats.getLastWorkDate();
        String workDateStr = workDate.format(DATE_FORMAT);

        if (lastWorkDateStr == null) {
            // 初めての作業
            stats.setCurrentStreak(1);
            stats.setTotalWorkDays(1);
            stats.setLastWorkDate(workDateStr);
        } else {
            LocalDate lastWorkDate = LocalDate.parse(lastWorkDateStr, DATE_FORMAT);
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(lastWorkDate, workDate);

            if (daysDiff == 0) {
                // 同じ日の追加作業（ストリークは変わらない）
            } else if (daysDiff == 1) {
                // 連続日
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
                stats.setTotalWorkDays(stats.getTotalWorkDays() + 1);
                stats.setLastWorkDate(workDateStr);
            } else if (daysDiff > 1) {
                // ストリークが途切れた
                stats.setCurrentStreak(1);
                stats.setTotalWorkDays(stats.getTotalWorkDays() + 1);
                stats.setLastWorkDate(workDateStr);
            }
            // daysDiff < 0 の場合は過去の日付なので無視
        }

        // 最長ストリークを更新
        if (stats.getCurrentStreak() > stats.getLongestStreak()) {
            stats.setLongestStreak(stats.getCurrentStreak());
        }
    }

    /**
     * 週次・月次の作業時間をリセット
     */
    private void checkAndResetPeriods(UserStats stats) {
        LocalDate today = LocalDate.now();
        
        // 週の開始日（月曜日）
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        String weekStartStr = weekStart.format(DATE_FORMAT);
        
        // 月の開始日
        LocalDate monthStart = today.withDayOfMonth(1);
        String monthStartStr = monthStart.format(DATE_FORMAT);

        // 週次リセット
        if (stats.getWeeklyStartDate() == null || !stats.getWeeklyStartDate().equals(weekStartStr)) {
            stats.setWeeklyWorkSeconds(0L);
            stats.setWeeklyStartDate(weekStartStr);
        }

        // 月次リセット
        if (stats.getMonthlyStartDate() == null || !stats.getMonthlyStartDate().equals(monthStartStr)) {
            stats.setMonthlyWorkSeconds(0L);
            stats.setMonthlyStartDate(monthStartStr);
        }
    }

    /**
     * ストリークが途切れているかチェック（毎日のチェック用）
     */
    @Transactional
    public void checkStreakBreak(User user) {
        UserStats stats = userStatsRepository.findByUser(user).orElse(null);
        if (stats == null || stats.getLastWorkDate() == null) {
            return;
        }

        LocalDate lastWorkDate = LocalDate.parse(stats.getLastWorkDate(), DATE_FORMAT);
        LocalDate today = LocalDate.now();
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(lastWorkDate, today);

        // 2日以上経過していればストリークリセット
        if (daysDiff > 1) {
            stats.setCurrentStreak(0);
            stats.setUpdatedAt(System.currentTimeMillis());
            userStatsRepository.save(stats);
        }
    }

    /**
     * 日別のアクティビティデータを取得（カレンダー用）
     * 過去365日分の作業時間を日付ごとに集計
     */
    public List<DailyStats> getDailyActivity(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusDays(365);
        
        // 過去365日分のWorkSessionを取得
        List<WorkSession> sessions = workSessionRepository.findByUserId(userId);
        
        // 日付ごとに作業時間を集計（分単位）
        Map<String, Integer> dailyMinutes = new HashMap<>();
        
        for (WorkSession session : sessions) {
            LocalDate sessionDate = LocalDate.parse(session.getWorkDate(), DATE_FORMAT);
            
            // 過去365日以内のデータのみ
            if (!sessionDate.isBefore(oneYearAgo) && !sessionDate.isAfter(today)) {
                String dateStr = session.getWorkDate();
                int minutes = (int) (session.getTimerSeconds() / 60);
                dailyMinutes.merge(dateStr, minutes, Integer::sum);
            }
        }
        
        // DailyStatsDTOのリストに変換
        return dailyMinutes.entrySet().stream()
            .map(entry -> new DailyStats(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(DailyStats::getDate))
            .collect(Collectors.toList());
    }
}
