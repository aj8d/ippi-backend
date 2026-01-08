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
    
    // ユーザーIDで作業セッションを取得
    List<WorkSession> findByUserId(Long userId);
    
    // ユーザーIDと作業日で作業セッションを取得
    Optional<WorkSession> findByUserIdAndWorkDate(Long userId, String workDate);
    
    // ユーザーIDで全作業セッションを削除
    void deleteByUserId(Long userId);
}
