package com.example.ippi.repository;

import com.example.ippi.entity.User;
import com.example.ippi.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    // ユーザーIDで統計を取得
    Optional<UserStats> findByUser(User user);

    // ユーザーIDで統計を取得
    Optional<UserStats> findByUserId(Long userId);
}
