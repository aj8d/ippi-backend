package com.example.ippi.repository;

import com.example.ippi.entity.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * WorkSessionRepository - 作業セッションのデータアクセス層
 */
@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {
    
    /**
     * ユーザーIDで作業セッションを検索
     */
    List<WorkSession> findByUserId(Long userId);
    
    /**
     * ユーザーIDと日付で作業セッションを検索
     */
    Optional<WorkSession> findByUserIdAndWorkDate(Long userId, String workDate);
    
    /**
     * ユーザーIDで作業セッションを削除
     */
    void deleteByUserId(Long userId);
}
