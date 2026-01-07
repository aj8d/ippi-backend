package com.example.ippi.entity;

import jakarta.persistence.*;

/**
 * ユーザーアチーブメント達成記録エンティティ
 * 
 * 📚 このエンティティの役割：
 * - どのユーザーがどのアチーブメントを達成したかを記録
 * - 達成日時を保存
 */
@Entity
@Table(name = "user_achievements", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "achieved_at", nullable = false)
    private Long achievedAt;

    public UserAchievement() {}

    public UserAchievement(User user, Achievement achievement, Long achievedAt) {
        this.user = user;
        this.achievement = achievement;
        this.achievedAt = achievedAt;
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

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }

    public Long getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(Long achievedAt) {
        this.achievedAt = achievedAt;
    }
}
