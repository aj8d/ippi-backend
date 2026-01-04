package com.example.ippi.service;

import com.example.ippi.entity.WorkSession;
import com.example.ippi.repository.WorkSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * WorkSessionService - 作業セッション管理サービス
 */
@Service
public class WorkSessionService {

    @Autowired
    private WorkSessionRepository workSessionRepository;

    /**
     * 作業セッションを保存または更新
     * 
     * @param userId ユーザーID
     * @param workDate 作業日（YYYY-MM-DD形式）
     * @param timerSeconds 作業時間（秒単位）
     * @return 保存されたWorkSession
     */
    @Transactional
    public WorkSession saveWorkSession(Long userId, String workDate, Long timerSeconds) {
        Optional<WorkSession> existingSession = workSessionRepository.findByUserIdAndWorkDate(userId, workDate);
        
        if (existingSession.isPresent()) {
            // 既存セッションがある場合は累積
            WorkSession session = existingSession.get();
            long currentSeconds = session.getTimerSeconds() != null ? session.getTimerSeconds() : 0L;
            session.setTimerSeconds(currentSeconds + timerSeconds);
            session.setUpdatedAt(System.currentTimeMillis());
            return workSessionRepository.save(session);
        } else {
            // 新規セッションを作成
            WorkSession newSession = new WorkSession(userId, workDate, timerSeconds);
            return workSessionRepository.save(newSession);
        }
    }

    /**
     * ユーザーの全作業セッションを取得
     */
    public List<WorkSession> getUserWorkSessions(Long userId) {
        return workSessionRepository.findByUserId(userId);
    }

    /**
     * 特定日の作業セッションを取得
     */
    public Optional<WorkSession> getWorkSessionByDate(Long userId, String workDate) {
        return workSessionRepository.findByUserIdAndWorkDate(userId, workDate);
    }
}
