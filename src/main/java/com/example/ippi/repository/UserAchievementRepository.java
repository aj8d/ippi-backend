package com.example.ippi.repository;

import com.example.ippi.entity.Achievement;
import com.example.ippi.entity.User;
import com.example.ippi.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    
    // ユーザーが達成したアチーブメント一覧を取得
    List<UserAchievement> findByUser(User user);
    
    // 特定のアチーブメントをユーザーが達成済みか確認
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
    
    // ユーザーが達成したアチーブメントの数を取得
    long countByUser(User user);
    
    // ユーザーが特定のアチーブメントを達成済みか（boolean）
    boolean existsByUserAndAchievement(User user, Achievement achievement);
    
    // ユーザーが達成したアチーブメントを達成日時順で取得
    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user = :user ORDER BY ua.achievedAt DESC")
    List<UserAchievement> findByUserOrderByAchievedAtDesc(@Param("user") User user);
}
