package com.example.ippi.controller;

import com.example.ippi.entity.TextData;
import com.example.ippi.service.TextDataService;
import com.example.ippi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/text-data")
public class TextDataController {

    @Autowired
    private TextDataService textDataService;
    
    @Autowired
    private UserRepository userRepository;

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
        Long userId = userRepository.findByEmail(email)
                .map(user -> user.getId())
                .orElse(null);
        
        Optional<TextData> existing = textDataService.getTextDataById(id);
        if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
            textDataService.deleteTextData(id);
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
}
