package com.example.ippi.service;

import com.example.ippi.entity.Achievement;
import com.example.ippi.entity.User;
import com.example.ippi.entity.UserAchievement;
import com.example.ippi.entity.UserStats;
import com.example.ippi.repository.AchievementRepository;
import com.example.ippi.repository.UserAchievementRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private ActivityService activityService;

    /**
     * アプリケーション起動時にアチーブメント定義を初期化
     */
    @PostConstruct
    @Transactional
    public void initializeAchievements() {
        // 既に登録済みならスキップ
        if (achievementRepository.count() > 0) {
            return;
        }

        List<Achievement> achievements = new ArrayList<>();
        int order = 0;

        // 時間系アチーブメント（秒単位）
        achievements.add(new Achievement(
            Achievement.TYPE_WORK_TIME, 
            "1時間達成", 
            "累計1時間の作業を達成しました", 
            3600L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_WORK_TIME, 
            "10時間達成", 
            "累計10時間の作業を達成しました", 
            36000L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_WORK_TIME, 
            "100時間達成", 
            "累計100時間の作業を達成しました", 
            360000L, 
            order++
        ));

        // 連続日数系アチーブメント
        achievements.add(new Achievement(
            Achievement.TYPE_STREAK, 
            "1日連続", 
            "1日連続で作業を続けています", 
            1L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_STREAK, 
            "7日連続", 
            "7日連続で作業を続けています", 
            7L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_STREAK, 
            "30日連続", 
            "30日連続で作業を続けています", 
            30L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_STREAK, 
            "100日連続", 
            "100日連続で作業を続けています", 
            100L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_STREAK, 
            "365日連続", 
            "365日連続で作業を続けています", 
            365L, 
            order++
        ));

        // タイマー使用回数系アチーブメント
        achievements.add(new Achievement(
            Achievement.TYPE_TIMER_COUNT, 
            "10回タイマー完了", 
            "タイマーを10回完了しました", 
            10L, 
            order++
        ));

        // Todo完了数系アチーブメント
        achievements.add(new Achievement(
            Achievement.TYPE_TODO_COUNT, 
            "10個Todo完了", 
            "Todoを10個完了しました", 
            10L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_TODO_COUNT, 
            "100個Todo完了", 
            "Todoを100個完了しました", 
            100L, 
            order++
        ));
        achievements.add(new Achievement(
            Achievement.TYPE_TODO_COUNT, 
            "1000個Todo完了", 
            "Todoを1000個完了しました", 
            1000L, 
            order++
        ));

        achievementRepository.saveAll(achievements);
    }

    /**
     * ユーザーのアチーブメント達成状況をチェックして、
     * 新規達成があればフィードに投稿
     */
    @Transactional
    public void checkAndAwardAchievements(User user, UserStats stats) {
        // 時間系チェック
        checkWorkTimeAchievements(user, stats.getTotalWorkSeconds());
        
        // 連続日数系チェック
        checkStreakAchievements(user, stats.getCurrentStreak());
        
        // タイマー回数系チェック
        checkTimerCountAchievements(user, stats.getTotalTimerSessions());
        
        // Todo完了数系チェック
        checkTodoCountAchievements(user, stats.getCompletedTodos());
    }

    /**
     * 時間系アチーブメントのチェック
     */
    private void checkWorkTimeAchievements(User user, Long totalWorkSeconds) {
        List<Achievement> workTimeAchievements = achievementRepository.findByType(Achievement.TYPE_WORK_TIME);
        
        for (Achievement achievement : workTimeAchievements) {
            if (totalWorkSeconds >= achievement.getThreshold()) {
                awardAchievementIfNew(user, achievement);
            }
        }
    }

    /**
     * 連続日数系アチーブメントのチェック
     */
    private void checkStreakAchievements(User user, Integer currentStreak) {
        List<Achievement> streakAchievements = achievementRepository.findByType(Achievement.TYPE_STREAK);
        
        for (Achievement achievement : streakAchievements) {
            if (currentStreak >= achievement.getThreshold()) {
                awardAchievementIfNew(user, achievement);
            }
        }
    }

    /**
     * タイマー回数系アチーブメントのチェック
     */
    private void checkTimerCountAchievements(User user, Integer totalTimerSessions) {
        List<Achievement> timerCountAchievements = achievementRepository.findByType(Achievement.TYPE_TIMER_COUNT);
        
        for (Achievement achievement : timerCountAchievements) {
            if (totalTimerSessions >= achievement.getThreshold()) {
                awardAchievementIfNew(user, achievement);
            }
        }
    }

    /**
     * Todo完了数系アチーブメントのチェック
     */
    private void checkTodoCountAchievements(User user, Integer completedTodos) {
        List<Achievement> todoCountAchievements = achievementRepository.findByType(Achievement.TYPE_TODO_COUNT);
        
        for (Achievement achievement : todoCountAchievements) {
            if (completedTodos >= achievement.getThreshold()) {
                awardAchievementIfNew(user, achievement);
            }
        }
    }

    /**
     * 未達成のアチーブメントを付与してフィードに投稿
     */
    private void awardAchievementIfNew(User user, Achievement achievement) {
        // 既に達成済みならスキップ
        if (userAchievementRepository.existsByUserAndAchievement(user, achievement)) {
            return;
        }

        // アチーブメント達成を記録
        UserAchievement userAchievement = new UserAchievement(
            user, 
            achievement, 
            System.currentTimeMillis()
        );
        userAchievementRepository.save(userAchievement);

        // フィードにアクティビティを投稿
        activityService.createAchievementActivity(
            user, 
            achievement.getName(), 
            achievement.getDescription()
        );
    }

    /**
     * ユーザーが達成したアチーブメント一覧を取得
     */
    public List<UserAchievement> getUserAchievements(User user) {
        return userAchievementRepository.findByUserOrderByAchievedAtDesc(user);
    }

    /**
     * 全アチーブメント定義を取得
     */
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAllByOrderByDisplayOrderAsc();
    }
}
