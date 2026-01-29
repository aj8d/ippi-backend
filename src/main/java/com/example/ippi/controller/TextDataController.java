package com.example.ippi.controller;

import com.example.ippi.entity.TextData;
import com.example.ippi.entity.User;
import com.example.ippi.entity.UserStats;
import com.example.ippi.entity.WorkSession;
import com.example.ippi.dto.WorkSessionRequest;
import com.example.ippi.service.TextDataService;
import com.example.ippi.service.ActivityService;
import com.example.ippi.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * テキストデータと作業時間のREST APIコントローラー
 * 
 * エンドポイント：
 * - GET    /api/text-data              - ユーザーの全データ取得
 * - GET    /api/text-data/{id}         - 特定のデータ取得
 * - POST   /api/text-data              - 新規作成
 * - PUT    /api/text-data/{id}         - 更新
 * - DELETE /api/text-data/{id}         - 削除
 * - POST   /api/text-data/work-session - 作業セッション保存
 */
@RestController
@RequestMapping("/text-data")
public class TextDataController {

    @Autowired
    private TextDataService textDataService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityService activityService;

    @Autowired
    private com.example.ippi.service.UserStatsService userStatsService;

    // GET: ログインユーザーのテキストデータを取得
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TextData>> getAllTextData(Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        if (userId == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<TextData> data = textDataService.getTextDataByUserId(userId);
        return ResponseEntity.ok(data);
    }

    // GET: ID でテキストデータを取得（所有者確認）
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> getTextDataById(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> data = textDataService.getTextDataById(id);
        if (data.isPresent() && data.get().getUserId().equals(userId)) {
            return ResponseEntity.ok(data.get());
        }
        return ResponseEntity.notFound().build();
    }

    // POST: 新しいテキストデータを作成（ユーザー ID を自動設定）
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> createTextData(@RequestBody TextData textData, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        textData.setUserId(userId);
        TextData savedData = textDataService.saveTextData(textData);
        return ResponseEntity.ok(savedData);
    }

    // PUT: テキストデータを更新（所有者確認）
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> updateTextData(
            @PathVariable Long id,
            @RequestBody TextData textData,
            Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> existing = textDataService.getTextDataById(id);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            TextData updatedData = textDataService.updateTextData(id, textData);
            return ResponseEntity.ok(updatedData);
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE: テキストデータを削除（所有者確認）
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTextData(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        Optional<TextData> existing = textDataService.getTextDataById(id);
        
        if (existing.isPresent() && existing.get().getUserId().equals(user.getId())) {
            textDataService.deleteTextData(id);
            
            // Todo完了の統計を更新
            userStatsService.recordTodoCompleted(user);
            
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // POST: タイマーを開始
    @PostMapping("/{id}/start-timer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> startTimer(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> existing = textDataService.getTextDataById(id);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            TextData data = existing.get();
            data.setTimerStartedAt(System.currentTimeMillis());
            data.setTimerRunning(true);
            TextData updatedData = textDataService.saveTextData(data);
            return ResponseEntity.ok(updatedData);
        }
        return ResponseEntity.notFound().build();
    }

    // PUT: タイマーを停止して経過時間を保存
    @PutMapping("/{id}/stop-timer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TextData> stopTimer(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> existing = textDataService.getTextDataById(id);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            TextData data = existing.get();
            if (data.getTimerRunning() && data.getTimerStartedAt() != null) {
                long elapsedMillis = System.currentTimeMillis() - data.getTimerStartedAt();
                long elapsedSeconds = elapsedMillis / 1000;
                // タイマー秒数は上書き（累積しない）
                data.setTimerSeconds(elapsedSeconds);
                data.setTimerRunning(false);
                data.setTimerStartedAt(null);
            }
            TextData updatedData = textDataService.saveTextData(data);
            return ResponseEntity.ok(updatedData);
        }
        return ResponseEntity.notFound().build();
    }

    // GET: タイマーの状態を確認（バックグラウンド復帰時用）
    @GetMapping("/{id}/timer-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTimerStatus(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> existing = textDataService.getTextDataById(id);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            TextData data = existing.get();
            Map<String, Object> status = new HashMap<>();
            status.put("id", data.getId());
            status.put("timerRunning", data.getTimerRunning());
            
            if (data.getTimerRunning() && data.getTimerStartedAt() != null) {
                // 実行中の場合、現在の経過秒数を計算して返す
                long elapsedSeconds = (System.currentTimeMillis() - data.getTimerStartedAt()) / 1000;
                status.put("elapsedSeconds", elapsedSeconds);
            } else {
                // 停止中の場合、保存されたタイマー秒数を返す
                status.put("elapsedSeconds", data.getTimerSeconds());
            }
            return ResponseEntity.ok(status);
        }
        return ResponseEntity.notFound().build();
    }

    // POST: タイマー完了を記録（今日のカウント加算）
    @PostMapping("/timer-completion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> recordTimerCompletion(Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        User user = userOpt.get();
        userStatsService.recordTimerCompletion(user);
        
        // 更新後の統計を返す
        UserStats stats = userStatsService.getOrCreateStats(user);
        return ResponseEntity.ok(Map.of(
            "dailyTimerCompletions", stats.getDailyTimerCompletions(),
            "lastCompletionDate", stats.getLastCompletionDate()
        ));
    }

    /**
     * 作業セッションの時間を保存
     * 
     * リクエスト形式：
     * POST /api/text-data/work-session
     * Content-Type: application/json
     * Authorization: Bearer {token}
     * 
     * {
     *   "date": "2024-12-31",
     *   "timerSeconds": 1500
     * }
     * 
     * @param request WorkSessionRequest（日付と作業秒数を含む）
     * @param principal 認証情報（ログインユーザー）
     * @return 保存されたWorkSession
     */
    @PostMapping("/work-session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorkSession> saveWorkSession(
            @Valid @RequestBody WorkSessionRequest request,
            Principal principal) {
        
        // Principal: Spring Securityが提供する認証情報
        // getName()でログインユーザーのメールアドレスを取得
        String email = principal.getName();
        
        // メールアドレスからユーザーを取得
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        // ユーザーが見つからない場合はエラー
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = userOpt.get();
        Long userId = user.getId();
        
        // 不正なデータがデータベースに保存されるのを防ぐ
        if (request.getDate() == null || request.getTimerSeconds() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 作業時間が0以下の場合は保存しない
        if (request.getTimerSeconds() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        // サービス層に処理を委譲
        WorkSession savedSession = textDataService.saveWorkSession(
                userId,
                request.getDate(),
                request.getTimerSeconds()
        );
        
        // ユーザー統計を更新
        userStatsService.recordWorkSession(user, request.getDate(), request.getTimerSeconds());
        
        // アクティビティを作成（フィード用）
        // 作業時間を分に変換してアクティビティを作成
        // 20分以上（1200秒以上）の作業のみフィードに投稿
        int minutes = (int) (request.getTimerSeconds() / 60);
        if (request.getTimerSeconds() >= 1200) {
            activityService.createWorkCompletedActivity(user, minutes);
        }
        
        // 保存されたデータを返す
        return ResponseEntity.ok(savedSession);
    }
}
