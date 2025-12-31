package com.example.ippi.repository;

import com.example.ippi.entity.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // 特定ユーザーのアクティビティを取得
    List<Activity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // フォローしているユーザーのアクティビティを取得（フィード用）
    @Query("SELECT a FROM Activity a WHERE a.user.id IN :userIds ORDER BY a.createdAt DESC")
    List<Activity> findByUserIdInOrderByCreatedAtDesc(@Param("userIds") List<Long> userIds, Pageable pageable);

    // 特定ユーザーの最新アクティビティを取得
    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<Activity> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);

    // 特定タイプのアクティビティを取得
    List<Activity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(Long userId, String activityType, Pageable pageable);
}
